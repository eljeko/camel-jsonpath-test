package com.redhat;

import java.util.concurrent.TimeUnit;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ParsingTests extends CamelTestSupport {

    private String json_one = "{ \"payload\": { \"id\": 1, \"first_name\": \"Marie\", \"last_name\": \"Rose\" } }";

    private String json_two = "{ \"payload\": { \"id\": 2, \"first_name\": \"Marie2\", \"last_name\": \"Rose2\" } }";

    private String json_three = "{ \"payload\": { \"id\": 3, \"first_name\": \"Marie3\", \"last_name\": \"Rose3\" } }";

    @Test
    public void testJsonPathConcat() throws Exception {
        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");

        //First call
        mockEndpoint.expectedBodiesReceived("Marie Rose");
        template.sendBody("direct:start", json_one);

        Exchange exchange = mockEndpoint.getReceivedExchanges().get(0);

        //Simple header set
        assertEquals("Marie",exchange.getMessage().getHeader("simple.value.first_name"));
        assertEquals("Rose",exchange.getMessage().getHeader("simple.value.last_name"));
        //$.concat result
        assertMockEndpointsSatisfied(3, TimeUnit.SECONDS);
        
        mockEndpoint.reset();

        //Second call
        mockEndpoint.expectedBodiesReceived("Marie2 Rose2");
        template.sendBody("direct:start", json_two);

        exchange = mockEndpoint.getReceivedExchanges().get(0);

        //Simple header set
        assertEquals("Marie2",exchange.getMessage().getHeader("simple.value.first_name"));
        assertEquals("Rose2",exchange.getMessage().getHeader("simple.value.last_name"));
        //$.concat result
        assertMockEndpointsSatisfied(3, TimeUnit.SECONDS);

        mockEndpoint.reset();

        //Third call
        mockEndpoint.expectedBodiesReceived("Marie3 Rose3");
        template.sendBody("direct:start", json_three);

        exchange = mockEndpoint.getReceivedExchanges().get(0);

        //Simple header set
        assertEquals("Marie3",exchange.getMessage().getHeader("simple.value.first_name"));
        assertEquals("Rose3",exchange.getMessage().getHeader("simple.value.last_name"));
        //$.concat result
        assertMockEndpointsSatisfied(3, TimeUnit.SECONDS);
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new MyRouteBuilder();
    }


    private class MyRouteBuilder extends RouteBuilder {
        @Override
        public void configure() throws Exception {
            //@formatter:off
            from("direct:start")
                    .unmarshal().json(JsonLibrary.Gson)
                    .setHeader("simple.value.first_name").jsonpath("$.payload.first_name")
                    .setHeader("simple.value.last_name").jsonpath("$.payload.last_name")
                    .setBody(jsonpath("$.concat($.payload.first_name,\" \",$.payload.last_name)"))
                    .log("> first_name: ${header.simple.value.first_name}")
                    .log("> last_name: ${header.simple.value.last_name}")
                    .log("> $.concat VALUE: ${body}")
                    .to("mock:result");
                    
            //@formatter:on
        }
    }
}
