/*
 * Copyright Inrupt Inc.
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
package com.inrupt.client.accessgrant;

import java.util.Collections;
import java.util.List;

/**
 * The response from a verification operation.
 */
public class AccessCredentialVerification {

    private final List<String> checks;
    private final List<String> warnings;
    private final List<String> errors;

    /**
     * Create a verification response.
     *
     * @param checks the checks that were performed
     * @param warnings any warnings from the verification operation
     * @param errors any errors from the verification operation
     */
    public AccessCredentialVerification(final List<String> checks, final List<String> warnings,
            final List<String> errors) {
        this.checks = makeImmutable(checks);
        this.warnings = makeImmutable(warnings);
        this.errors = makeImmutable(errors);
    }

    /**
     * The verification checks that were performed.
     *
     * @return an unmodifiable list of any verification checks performed, never {@code null}
     */
    public List<String> getChecks() {
        return checks;
    }

    /**
     * The verification warnings that were discovered.
     *
     * @return an unmodifiable list of any verification warnings, never {@code null}
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * The verification errors that were discovered.
     *
     * @return an unmodifiable list of any verification errors, never {@code null}
     */
    public List<String> getErrors() {
        return errors;
    }

    static List<String> makeImmutable(final List<String> list) {
        if (list != null) {
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }
}

