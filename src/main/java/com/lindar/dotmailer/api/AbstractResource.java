package com.lindar.dotmailer.api;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import com.lindar.dotmailer.util.ErrorTranslator;
import com.lindar.dotmailer.vo.internal.DMAccessCredentials;
import com.lindar.dotmailer.vo.internal.ErrorResponse;
import com.lindar.wellrested.WellRestedRequest;
import com.lindar.wellrested.json.GsonJsonMapper;
import com.lindar.wellrested.vo.Result;
import com.lindar.wellrested.vo.ResultBuilder;
import com.lindar.wellrested.vo.WellRestedResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractResource {
    private static final String ERROR_INPUT = "ERROR_INPUT";
    private static final String ERROR_UNKNOWN = "ERROR_UNKNOWN";


    static final String WITH_FULL_DATA_ATTR = "withFullData";
    static final String DM_DATE_FORMAT = "yyyy-MM-dd";
    static final String DM_DATE_TIME_FORMAT = "yyyy-MM-dd%20HH:mm:ss";

    private static final String DM_SELECT_SKIP_ATTRIBUTES = "select=%s&skip=%s";
    private static final String AND = "&";
    private static final String QUESTION = "?";
    private static final String EQUAL = "=";
    static final int DEFAULT_MAX_SELECT = 1000;
    static final int DEFAULT_PER_STEP = 1000;
    static final int MAX_CONTACTS_TO_PROCESS_PER_STEP = 50000;

    @Getter
    private long recordsSynced = 0L;

    @Getter
    private final DMAccessCredentials accessCredentials;

    AbstractResource(DMAccessCredentials accessCredentials) {
        this.accessCredentials = accessCredentials;
    }

    private String dotmailerUrl() {
        return accessCredentials.getApiUrl() + accessCredentials.getVersion();
    }

    private WellRestedRequest buildRequestFromResourcePath(String resourcePath) {
        String url = validatePath(dotmailerUrl() + resourcePath);
        return WellRestedRequest.builder().url(url).credentials(accessCredentials.getUsername(), accessCredentials.getPassword()).build();
    }

    private String validatePath(String path) {
        String newPath = path.replaceAll("//", "/");
        return newPath.replaceFirst("/", "//");
    }

    private String validatePathWithAttributes(String path) {
        String newPath = validatePath(path);
        if (newPath.endsWith("/")) {
            newPath = newPath.substring(0, newPath.length() - 1);
            newPath += QUESTION;
        } else if (newPath.contains(QUESTION)) {
            newPath += AND;
        } else {
            newPath += QUESTION;
        }
        return newPath.replaceAll(AND + AND, AND);
    }

    <T> Result<T> sendAndGet(String resourcePath, Class<T> clazz) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.get().submit();
        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo(clazz));
        }
        return parseErrorResponse(response);
    }

    <T> Result<T> postAndGet(String resourcePath, T objectToPost) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.post().jsonContent(objectToPost).submit();
        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo(((Class<T>) objectToPost.getClass())));
        } else {
            log.error("Dotmailer Error: {} ", request.get().submit().getServerResponse());
        }
        return parseErrorResponse(response);
    }

    protected <T> Result<T> postAndGet(String resourcePath, Object objectToPost, Class<T> responseClass) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.post().jsonContent(objectToPost).submit();
        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo(responseClass));
        }
        return parseErrorResponse(response);
    }

    protected Result<Void> postAndGetBlankResponse(String resourcePath, Object objectToPost) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.post().jsonContent(objectToPost).submit();
        if (validBlankResponse(response)) {
            return ResultBuilder.successful().buildAndIgnoreData();
        }
        return parseErrorResponse(response);
    }

    int post(String resourcePath, Object objectToPost) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.post().jsonContent(objectToPost).submit();
        return response.getStatusCode();
    }

    <T> Result<T> postFileAndGet(String resourcePath, String filePath, Class<T> responseClass) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);

        File postedFile = new File(filePath);
        WellRestedResponse response = request.post().file(postedFile).submit();
        postedFile.delete();

        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo(responseClass));
        }
        return parseErrorResponse(response);
    }

    <T> Result<T> putAndGet(String resourcePath, T objectToPost) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.put().jsonContent(objectToPost).submit();
        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo((Class<T>) objectToPost.getClass()));
        }
        return parseErrorResponse(response);
    }

    Result delete(String resourcePath) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.delete().submit();
        if (validBlankResponse(response)) {
            return ResultBuilder.successful(true);
        }
        return parseErrorResponse(response);
    }

    <T> Result<List<T>> sendAndGetFullList(String resourcePath, TypeToken<List<T>> typeToken) {
        return sendAndGetFullList(resourcePath, typeToken, DEFAULT_MAX_SELECT, 0);
    }

    protected <T> Result<List<T>> sendAndGetFullList(String resourcePath, TypeToken<List<T>> typeToken, int maxSelect) {
        return sendAndGetFullList(resourcePath, typeToken, maxSelect, 0);
    }

    <T> Result<List<T>> sendAndGetFullList(String resourcePath, TypeToken<List<T>> typeToken, int maxSelect, int limit) {
        return sendAndGetFullList(resourcePath, typeToken, maxSelect, limit);
    }

    <T> Result<List<T>> sendAndGetFullList(String resourcePath, TypeToken<List<T>> typeToken, int maxSelect, int limit, int initialSkip) {

        if (resourcePath.contains("select") && resourcePath.contains("skip")) {
            return ResultBuilder.failed()
                    .msg("Please remove select and skip attributes from the URL if you want to select the full list. Otherwise try the single list method")
                    .code(ERROR_INPUT).buildAndIgnoreData();
        }
        String baseUrl = validatePathWithAttributes(dotmailerUrl() + resourcePath);

        int skip = -maxSelect + initialSkip;
        int newResultsSize = 0;
        List<T> allResults = new ArrayList<>(DEFAULT_MAX_SELECT);
        do {
            try {
                skip += maxSelect;
                String url = baseUrl + String.format(DM_SELECT_SKIP_ATTRIBUTES, maxSelect, skip);
                log.trace("GeneratedUrl: {}", url);
                WellRestedRequest request = WellRestedRequest.builder().url(url)
                                                             .credentials(accessCredentials.getUsername(), accessCredentials.getPassword()).build();
                WellRestedResponse response = request.get().submit();
                if (validResponse(response)) {
                    List<T> newResults = response.fromJson().castTo(typeToken);
                    allResults.addAll(newResults);
                    newResultsSize = newResults.size();

                    if (newResults.size() < maxSelect) {
                        break;
                    }
                } else {
                    return ResultBuilder.of(parseErrorResponse(response)).buildAndOverrideData(allResults);
                }
            } catch (Exception ex) {
                log.error("sendAndGetFullList: error occurred: ", ex);
                return ResultBuilder.failed().msg(ex.getMessage()).code(ERROR_UNKNOWN).buildAndIgnoreData();
            }
        } while (newResultsSize != 0 && (limit <= 0 || allResults.size() < limit));
        return ResultBuilder.successful(allResults);
    }

    protected <T> Result<List<T>> sendAndGetSingleList(String resourcePath, TypeToken<List<T>> typeToken) {
        WellRestedRequest request = buildRequestFromResourcePath(resourcePath);
        WellRestedResponse response = request.get().submit();
        if (validResponse(response)) {
            return ResultBuilder.successful(response.fromJson().castTo(typeToken));
        }
        return parseErrorResponse(response);
    }

    String pathWithId(String path, Long id) {
        return String.format(path, id);
    }

    String pathWithParam(String path, String param) {
        return String.format(path, param);
    }

    String pathWithIdAndParam(String path, Long id, String param) {
        return String.format(path, id, param);
    }

    String addAttrAndValueToPath(String path, String attrName, String value) {
        String newPath = validatePathWithAttributes(path);
        return newPath + attrName + EQUAL + value;
    }

    private boolean validResponse(WellRestedResponse response) {
        return validBlankResponse(response) && StringUtils.isNotBlank(response.getServerResponse());
    }

    private boolean validBlankResponse(WellRestedResponse response) {
        return response != null && (response.getStatusCode() == 200 || response.getStatusCode() == 201 || response.getStatusCode() == 202);
    }

    private <T> Result<T> parseErrorResponse(WellRestedResponse response) {
        ErrorResponse errorResponse = response.fromJson().castTo(ErrorResponse.class);
        if (errorResponse == null || errorResponse.getMessage() == null || StringUtils.isBlank(errorResponse.getMessage())) {
            return ResultBuilder.failed().msg("Unknown Error").code("UNKNOWN_ERROR").buildAndIgnoreData();
        }

        String errorCode = "UNKNOWN_ERROR";
        String errorMessage = errorResponse.getMessage();
        if (errorResponse.getMessage().startsWith("Error: ")) {
            errorCode = errorMessage.substring(7);
            errorMessage = ErrorTranslator.getInstance().translate(errorCode);
        }

        return ResultBuilder.failed().msg(errorMessage).code(errorCode).buildAndIgnoreData();
    }

}
