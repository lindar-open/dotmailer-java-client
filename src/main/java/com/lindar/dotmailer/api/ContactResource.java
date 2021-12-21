package com.lindar.dotmailer.api;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.lindar.dotmailer.util.CsvUtil;
import com.lindar.dotmailer.util.DefaultEndpoints;
import com.lindar.dotmailer.util.PersonalizedContactsProcessFunction;
import com.lindar.dotmailer.vo.api.AddressBook;
import com.lindar.dotmailer.vo.api.Contact;
import com.lindar.dotmailer.vo.api.JobReport;
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
import java.util.function.Consumer;

@Slf4j
public class ContactResource extends AbstractResource {
    
    public ContactResource(DMAccessCredentials accessCredentials) {
        super(accessCredentials);
    }
    
    public Result<Contact> get(Long id) {
        return sendAndGet(pathWithId(DefaultEndpoints.CONTACT.getPath(), id), Contact.class);
    }
    
    public Result<Contact> get(String email) {
        return sendAndGet(pathWithParam(DefaultEndpoints.CONTACT.getPath(), email), Contact.class);
    }
    
    /**
     * Gets a list of all contacts in the account
     * DEFAULT ATTR: No limit set and without full data
     * @return
     */
    public Result<List<Contact>> list() {
        return list(false, 0);
    }
    
    /**
     * Gets a list of all contacts in the account
     * DEFAULT ATTR: No limit set
     * @param withFullData
     * @return
     */
    public Result<List<Contact>> list(boolean withFullData) {
        return list(withFullData, 0);
    }
    
    public Result<List<Contact>> list(boolean withFullData, int limit) {
        String initialPath = addAttrAndValueToPath(DefaultEndpoints.CONTACTS.getPath(), WITH_FULL_DATA_ATTR, BooleanUtils.toStringTrueFalse(withFullData));
        
        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(initialPath, new TypeToken<List<Contact>>() {}, maxSelect, limit);
        }
        return sendAndGetFullList(initialPath, new TypeToken<List<Contact>>() {});
    }
    
    /**
     * Gets a list of all contacts in the account created since the passed date
     * DEFAULT ATTR: No limit set and without full data
     * @param createdSince
     * @return
     */
    public Result<List<Contact>> list(Date createdSince) {
        return list(createdSince, false, 0);
    }
    
    /**
     * Gets a list of all contacts in the account created since the passed date
     * DEFAULT ATTR: No limit set
     * @param createdSince
     * @param withFullData
     * @return
     */
    public Result<List<Contact>> list(Date createdSince, boolean withFullData) {
        return list(createdSince, withFullData, 0);
    }


    /**
     * Gets a list of all contacts in the account created since the passed date
     * @param createdSince
     * @param withFullData
     * @param limit
     * @return
     */
    public Result<List<Contact>> list(Date createdSince, boolean withFullData, int limit) {
        return list(createdSince, withFullData, limit, true);
    }

    /**
     * Gets a list of all contacts in the account created since the passed date
     * @param createdSince
     * @param withFullData
     * @param limit
     * @param roundToDate
     * @return
     */
    public Result<List<Contact>> list(Date createdSince, boolean withFullData, int limit, boolean roundToDate) {
        String dateTemplate = roundToDate ? DM_DATE_FORMAT : DM_DATE_TIME_FORMAT;
        String rootPath = pathWithParam(DefaultEndpoints.CONTACTS_SINCE_DATE.getPath(), new DateTime(createdSince).toString(dateTemplate));
        String path = addAttrAndValueToPath(rootPath, WITH_FULL_DATA_ATTR, BooleanUtils.toStringTrueFalse(withFullData));
        
        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(path, new TypeToken<List<Contact>>() {}, maxSelect, limit);
        }
        return sendAndGetFullList(path, new TypeToken<List<Contact>>() {});
    }

    /**
     * Gets a list of unsubscribed contacts who unsubscribed after a given date
     * DEFAULT ATTRS: no limit
     * @param since
     * @return
     */
    public Result<List<SuppressedContact>> listUnsubscribed(Date since) {
        return listUnsubscribed(since, 0);
    }
    
    /**
     * Gets a list of unsubscribed contacts who unsubscribed after a given date
     * @param since
     * @param limit
     * @return
     */
    public Result<List<SuppressedContact>> listUnsubscribed(Date since, int limit) {
        String rootPath = pathWithParam(DefaultEndpoints.CONTACTS_UNSUBSCRIBED_SINCE_DATE.getPath(), new DateTime(since).toString(DM_DATE_FORMAT));
        
        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(rootPath, new TypeToken<List<SuppressedContact>>() {}, maxSelect, limit);
        }
        return sendAndGetFullList(rootPath, new TypeToken<List<SuppressedContact>>() {});
    }
    
    
    public Result<List<SuppressedContact>> listSuppressed(Date since) {
        return listSuppressed(since, 0);
    }
    
    
    /**
     * Gets a list of suppressed contacts after a given date along with the reason for suppression.
     * 
     * The possible suppression reasons can be:
        'Unsubscribed' - The contact is unsubscribed from your communications
        'SoftBounced' - The contact’s address is temporarily unavailable, possibly because their mailbox is too full, or their mail server won’t accept a message of the size sent, or their server is having temporary issues accepting any mail
        'HardBounced' - The contact’s address is permanently unreachable, most likely because they, or the server they were hosted on, does not exist
        'IspComplained' - The contact has submitted a spam complaint to us via their internet service provider
        'MailBlocked' - The mail server indicated that it didn’t want to receive the mail. No reason was given
        'DirectComplaint' - The contact has complained directly to either us, a hosting facility or possibly even a blacklist about receiving your communications
        'Suppressed' - The contact that has been actively suppressed by you in your account
        'NotAllowed' - The contact's email address is fully blocked from our system
        'DomainSuppression' - The contact’s email domain is on your domain suppression list
        'NoMxRecord' - The contact’s email domain does not have an MX DNS record. A mail exchange record provides the address of the mail server for that domain.
     * 
     * @param since
     * @param limit
     * @return
     */
    public Result<List<SuppressedContact>> listSuppressed(Date since, int limit) {
        return listSuppressed(since, limit, true);
    }


    /**
     * Gets a list of suppressed contacts after a given date along with the reason for suppression.
     *
     * The possible suppression reasons can be:
     'Unsubscribed' - The contact is unsubscribed from your communications
     'SoftBounced' - The contact’s address is temporarily unavailable, possibly because their mailbox is too full, or their mail server won’t accept a message of the size sent, or their server is having temporary issues accepting any mail
     'HardBounced' - The contact’s address is permanently unreachable, most likely because they, or the server they were hosted on, does not exist
     'IspComplained' - The contact has submitted a spam complaint to us via their internet service provider
     'MailBlocked' - The mail server indicated that it didn’t want to receive the mail. No reason was given
     'DirectComplaint' - The contact has complained directly to either us, a hosting facility or possibly even a blacklist about receiving your communications
     'Suppressed' - The contact that has been actively suppressed by you in your account
     'NotAllowed' - The contact's email address is fully blocked from our system
     'DomainSuppression' - The contact’s email domain is on your domain suppression list
     'NoMxRecord' - The contact’s email domain does not have an MX DNS record. A mail exchange record provides the address of the mail server for that domain.
     *
     * @param since
     * @param limit
     * @param roundToDate
     * @return
     */
    public Result<List<SuppressedContact>> listSuppressed(Date since, int limit, boolean roundToDate) {
        String dateTemplate = roundToDate ? DM_DATE_FORMAT : DM_DATE_TIME_FORMAT;
        String rootPath = pathWithParam(DefaultEndpoints.CONTACTS_SUPPRESSED_SINCE_DATE.getPath(), new DateTime(since).toString(dateTemplate));

        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(rootPath, new TypeToken<List<SuppressedContact>>() {}, maxSelect, limit);
        }
        return sendAndGetFullList(rootPath, new TypeToken<List<SuppressedContact>>() {});
    }

    /**
     * Gets any address books that a contact is in.
     * DEFAULT ATTR: No limit set
     * @param contactId
     * @return
     */
    public Result<List<AddressBook>> listAddressBooks(Long contactId) {
        return listAddressBooks(contactId, 0);
    }
    
    /**
     * Gets any address books that a contact is in.
     * @param contactId
     * @param limit
     * @return
     */
    public Result<List<AddressBook>> listAddressBooks(Long contactId, int limit) {
        String path = pathWithId(DefaultEndpoints.CONTACT_ADDRESS_BOOKS.getPath(), contactId);
        
        if (limit > 0) {
            int maxSelect = limit >= DEFAULT_MAX_SELECT ? DEFAULT_MAX_SELECT : limit;
            return sendAndGetFullList(path, new TypeToken<List<AddressBook>>() {}, maxSelect, limit);
        }
        return sendAndGetFullList(path, new TypeToken<List<AddressBook>>() {});
    }
    
    
    public Result<Contact> create(Contact newContact) {
        return postAndGet(DefaultEndpoints.CONTACTS.getPath(), newContact);
    }
    
    public Result<Contact> update(Contact updatedContact) {
        return putAndGet(DefaultEndpoints.CONTACTS.getPath(), updatedContact);
    }
    
    public Result delete(Long contactId) {
        return delete(pathWithId(DefaultEndpoints.CONTACT.getPath(), contactId));
    }
    
    /**
     * Bulk creates, or bulk updates, contacts.
     * @param <T>
     * @param customContactObjects
     * @return
     */
    public <T> Result<JobStatus> importList(List<T> customContactObjects) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.failed().msg(csvFilePath.getMsg()).code(csvFilePath.getCode()).buildAndIgnoreData();
        }
        return postFileAndGet(DefaultEndpoints.CONTACTS_IMPORT.getPath(), csvFilePath.getData(), JobStatus.class);
    }
    
    public <T> Result<JobStatus> importList(List<T> customContactObjects, List<String> csvHeaders) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.failed().msg(csvFilePath.getMsg()).code(csvFilePath.getCode()).buildAndIgnoreData();
        }
        return postFileAndGet(DefaultEndpoints.CONTACTS_IMPORT.getPath(), csvFilePath.getData(), JobStatus.class);
    }
    
    public <T> Result<JobStatus> importList(List<T> customContactObjects, List<String> csvHeaders, List<String> fieldNames) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders, fieldNames);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.failed().msg(csvFilePath.getMsg()).code(csvFilePath.getCode()).buildAndIgnoreData();
        }
        return postFileAndGet(DefaultEndpoints.CONTACTS_IMPORT.getPath(), csvFilePath.getData(), JobStatus.class);
    }
    
    public <T> Result<JobStatus> importList(List<T> customContactObjects, List<String> csvHeaders, List<String> fieldNames, CellProcessor[] cellProcessors) {
        Result<String> csvFilePath = CsvUtil.writeCsv(customContactObjects, csvHeaders, fieldNames, cellProcessors);
        if (!csvFilePath.isSuccessAndNotNull()) {
            return ResultBuilder.failed().msg(csvFilePath.getMsg()).code(csvFilePath.getCode()).buildAndIgnoreData();
        }
        return postFileAndGet(DefaultEndpoints.CONTACTS_IMPORT.getPath(), csvFilePath.getData(), JobStatus.class);
    }
    
    public Result<JobStatus> getImportStatus(String guid) {
        return sendAndGet(pathWithParam(DefaultEndpoints.CONTACTS_IMPORT_STATUS.getPath(), guid), JobStatus.class);
    }
    
    public Result<JobReport> getImportReport(String guid) {
        return sendAndGet(pathWithParam(DefaultEndpoints.CONTACTS_IMPORT_REPORT.getPath(), guid), JobReport.class);
    }
}
