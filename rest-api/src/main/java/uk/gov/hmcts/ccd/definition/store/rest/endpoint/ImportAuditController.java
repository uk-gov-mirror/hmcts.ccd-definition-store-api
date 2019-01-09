package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.definition.store.rest.model.ImportAudit;
import uk.gov.hmcts.ccd.definition.store.rest.service.AzureImportAuditsClient;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping(value = "/api")
@Slf4j
class ImportAuditController {

    private final AzureImportAuditsClient azureImportAuditsClient;

    @Autowired
    ImportAuditController(@Autowired(required = false) final AzureImportAuditsClient azureImportAuditsClient) {
        this.azureImportAuditsClient = azureImportAuditsClient;
    }

    @GetMapping(value = "/import-audits", produces = {"application/json"})
    @ApiOperation(value = "Fetches import audits")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Import audits")
    })
    Collection<ImportAudit> fetchAllAudits() throws StorageException {
        if (null != azureImportAuditsClient) {
            return azureImportAuditsClient.fetchAllImportAudits();
        } else {
            return emptyList();
        }
    }

    @GetMapping(value = "/definitionlob/{id}")
    @ApiOperation("Gets the definition blob content by id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns definition content of a blob")
    })
    public ResponseEntity<Object> getDefinitionBlobContent(
        @PathVariable String id,
        HttpServletResponse response) {

//        response.setHeader(CONTENT_TYPE, documentContentVersion.getMimeType());
//        response.setHeader(HttpHeaders.CONTENT_LENGTH, documentContentVersion.getSize().toString());
//        response.setHeader("OriginalFileName", documentContentVersion.getOriginalDocumentName());
//        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
//                           String.format("fileName=\"%s\"", documentContentVersion.getOriginalDocumentName()));

        try {
            final CloudBlockBlob blob = azureImportAuditsClient.loadBlob(id);
            blob.download(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException | StorageException | URISyntaxException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e);
        }

        return null;
    }
}
