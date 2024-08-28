/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.de.pipeline.exceptions

class PipelineLibraryException extends RuntimeException {

    PipelineLibraryException(final String message) {
        super(message)
    }

    PipelineLibraryException(final Throwable cause) {
        super(cause)
    }

}
