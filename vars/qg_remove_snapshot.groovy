#!/usr/bin/env groovy
import com.ericsson.oss.de.pipeline.exceptions.PipelineLibraryException

def call(args) {

    if (!args?.version) {
        throw new PipelineLibraryException("The version needs to be provided to remove the SNAPSHOT.")
    }

    if (args.version?.endsWith("-SNAPSHOT"))
        return args.version.split("-")[0]
    else
        return args.version

}