/*
 * Copyright 2022 Inrupt Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.inrupt.client.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.inrupt.client.VerifiableCredential;
import com.inrupt.client.spi.JsonService;
import com.inrupt.client.spi.ServiceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class JsonVCSerializeDeserialize {

    private VerifiableCredential vc;
    private VerifiableCredential vcCopy;
    private static final JsonService service = ServiceProvider.getJsonService();

    @Test
    void roundtripVC() throws IOException {
        try (final var res = JsonVCSerializeDeserialize.class
                .getResourceAsStream("/com/inrupt/client/test/json/verifiableCredential.json")) {
            vc = service.fromJson(res, VerifiableCredential.class);
        }

        final var targetPath = new File("target").toPath();
        final var testFolderName = UUID.randomUUID().toString();
        final var testFolderPath = Files.createTempDirectory(targetPath, testFolderName);
        final var testFile =
                Files.createTempFile(testFolderPath, UUID.randomUUID().toString(), ".json");

        try (final var out = new FileOutputStream(testFile.toString())) {
            service.toJson(vc, out);
        }

        try (final var in = new FileInputStream(testFile.toString())) {
            vcCopy = service.fromJson(in, VerifiableCredential.class);
        }

        assertEquals(vc.context, vcCopy.context);
        assertEquals(vc.id, vcCopy.id);
        assertEquals(vc.type, vcCopy.type);
        assertEquals(vc.issuer, vcCopy.issuer);
        assertEquals(vc.issuanceDate, vcCopy.issuanceDate);
        assertEquals(vc.expirationDate, vcCopy.expirationDate);
        assertEquals(vc.credentialSubject, vcCopy.credentialSubject);
        assertEquals(vc.credentialStatus, vcCopy.credentialStatus);
        assertEquals(vc.proof, vcCopy.proof);

        Files.deleteIfExists(testFile);
        Files.deleteIfExists(testFolderPath);

    }

}
