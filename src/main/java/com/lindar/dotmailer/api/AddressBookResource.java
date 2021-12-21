package com.lindar.dotmailer.api;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.lindar.dotmailer.util.CsvUtil;
import com.lindar.dotmailer.util.DefaultEndpoints;
import com.lindar.dotmailer.util.PersonalizedContactsProcessFunction;
import com.lindar.dotmailer.vo.api.AddressBook;
import com.lindar.dotmailer.vo.api.Contact;
import com.lindar.dotmailer.vo.api.JobStatus;
import com.lindar.dotmailer.vo.api.PersonalisedContact;
import com.lindar.dotmailer.vo.api.SuppressedContact;
import com.lindar.dotmailer.vo.internal.DMAccessCredentials;
import com.lindar.wellrested.vo.Result;
import com.lindar.wellrested.vo.ResultBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.Date;
import java.util.List;

@Slf4j
public class AddressBookResource extends AbstractResource {

    public AddressBookResource(DMAccessCredentials accessCredentials) {
        super(accessCredentials);
    }

    public Result<List<AddressBook>> list() {
        return sendAndGetFullList(DefaultEndpoints.ADDRESS_BOOKS.getPath(), new TypeToken<List<AddressBook>>() {});
    }

    public Result<AddressBook> get(Long addressBookId) {
        String path = pathWithId(DefaultEndpoints.ADDRESS_BOOK.getPath(), addressBookId);
        return sendAndGet(path, AddressBook.class);
    }

    public Result<AddressBook> create(AddressBook addressBook) {
        String path = DefaultEndpoints.ADDRESS_BOOKS.getPath();
        return postAndGet(path, addressBook);
    }

    public Result<Contact> addContact(Long addressBookId, Contact contact) {
        String path = pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS.getPath(), addressBookId);
        return postAndGet(path, contact);
    }

    public Result<JobStatus> deleteContacts(Long addressBookId, List<Long> contactIds) {
        String path = pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_DELETE.getPath(), addressBookId);
        return postAndGet(path, contactIds, JobStatus.class);
    }

    /**
     * List address book contacts. DEFAULT ATTRS: No limit and without full data
     */
    public Result<List<Contact>> listContacts(Long addressBookId) {
        return listContacts(addressBookId, false, 0);
    }

    public Result<List<Contact>> listContacts(Long addressBookId, Boolean withFullData) {
        return listContacts(addressBookId, withFullData, 0);
    }

    public Result<List<Contact>> listContacts(Long addressBookId, Boolean withFullData, int limit) {
        String initialPath = addAttrAndValueToPath(DefaultEndpoints.ADDRESS_BOOK_CONTACTS.getPath(), WITH_FULL_DATA_ATTR, BooleanUtils.toString(withFullData, "true", "false", "false"));
        String path = pathWithId(initialPath, addressBookId);

        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(path, new TypeToken<List<Contact>>() {
            }, maxSelect, limit);
        }
        return sendAndGetFullList(path, new TypeToken<List<Contact>>() {
        });
    }

    /**
     * Gets a list of contacts who have unsubscribed since a given date from a given address book. No limit
     */
    public Result<List<SuppressedContact>> listUnsubscribedContacts(Long addressBookId, Date since) {
        return listUnsubscribedContacts(addressBookId, since, 0);
    }

    /**
     * Gets a list of contacts who have unsubscribed since a given date from a given address book
     *
     * @param addressBookId
     * @param since
     * @param limit
     * @return
     */
    public Result<List<SuppressedContact>> listUnsubscribedContacts(Long addressBookId, Date since, int limit) {
        String path = pathWithIdAndParam(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_UNSUBSCRIBED_SINCE_DATE.getPath(), addressBookId, new DateTime(since).toString(DM_DATE_FORMAT));

        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(path, new TypeToken<List<SuppressedContact>>() {
            }, maxSelect, limit);
        }
        return sendAndGetFullList(path, new TypeToken<List<SuppressedContact>>() {});
    }

    /**
     * Bulk creates, or bulk updates, contacts.
     *
     * @param <T>
     * @param addressBookId
     * @param customContactObjects
     * @return
     */
    public <T> Result<JobStatus> importList(Long addressBookId, List<T> customContactObjects) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.of(csvFilePath).buildAndIgnoreData();
        }
        return postFileAndGet(pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_IMPORT.getPath(), addressBookId), csvFilePath.getData(), JobStatus.class);
    }

    public <T> Result<JobStatus> importList(Long addressBookId, List<T> customContactObjects, List<String> csvHeaders) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.of(csvFilePath).buildAndIgnoreData();
        }
        return postFileAndGet(pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_IMPORT.getPath(), addressBookId), csvFilePath.getData(), JobStatus.class);
    }

    public <T> Result<JobStatus> importList(Long addressBookId, List<T> customContactObjects, List<String> csvHeaders, List<String> fieldNames) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders, fieldNames);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.of(csvFilePath).buildAndIgnoreData();
        }
        return postFileAndGet(pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_IMPORT.getPath(), addressBookId), csvFilePath.getData(), JobStatus.class);
    }

    public <T> Result<JobStatus> importList(Long addressBookId, List<T> customContactObjects, List<String> csvHeaders, List<String> fieldNames, CellProcessor[] cellProcessors) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders, fieldNames, cellProcessors);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.of(csvFilePath).buildAndIgnoreData();
        }
        return postFileAndGet(pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_IMPORT.getPath(), addressBookId), csvFilePath.getData(), JobStatus.class);
    }

    public <T> Result<JobStatus> importList(Long addressBookId, String csvFilePath) {
        return postFileAndGet(pathWithId(DefaultEndpoints.ADDRESS_BOOK_CONTACTS_IMPORT.getPath(), addressBookId), csvFilePath, JobStatus.class);
    }
}
