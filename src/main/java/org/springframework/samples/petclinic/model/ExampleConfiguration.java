package org.springframework.samples.petclinic.model;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * All SDK management takes place here, away from the instrumentation code, which should
 * only access the OpenTelemetry APIs.
 */
public final class ExampleConfiguration {

    // Name of the service
    private static final String SERVICE_NAME = "myExampleService1";

    /**
     * Adds a SimpleSpanProcessor initialized with ZipkinSpanExporter to the
     * TracerSdkProvider
     */
    public static OpenTelemetry initializeOpenTelemetry() {
        String endpoint = String.format("http://%s:%s/api/v2/spans", "localhost", 9411);
        ZipkinSpanExporter zipkinExporter = ZipkinSpanExporter.builder().setEndpoint(endpoint).build();

        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, SERVICE_NAME));

        // Set to process the spans by the Zipkin Exporter
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(zipkinExporter))
                .setResource(Resource.getDefault().merge(serviceNameResource)).build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();

        // add a shutdown hook to shut down the SDK
        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

        // return the configured instance so it can be used for instrumentation.
        return openTelemetry;
    }

}
