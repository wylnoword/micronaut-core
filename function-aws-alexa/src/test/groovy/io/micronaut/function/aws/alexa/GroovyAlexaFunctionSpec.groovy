package io.micronaut.function.aws.alexa

import com.amazon.ask.Skills
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.ResponseEnvelope
import com.amazon.ask.model.services.Serializer
import com.amazon.ask.util.JacksonSerializer
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.micronaut.function.aws.MicronautRequestHandler
import io.micronaut.function.aws.alexa.handlers.CancelandStopIntentHandler
import io.micronaut.function.aws.alexa.handlers.HelloWorldIntentHandler
import io.micronaut.function.aws.alexa.handlers.HelpIntentHandler
import io.micronaut.function.aws.alexa.handlers.LaunchRequestHandler
import io.micronaut.function.aws.alexa.handlers.SessionEndedRequestHandler
import spock.lang.Specification


class GroovyAlexaFunctionSpec extends Specification {

        void "test micronaut request handler"() {
            given:
            def file = new File("src/test/groovy/io/micronaut/function/aws/alexa/inputEnvelope.json").newInputStream()
            Serializer serializer = new JacksonSerializer()
            RequestEnvelope requestEnvelope = serializer.deserialize(file as InputStream,RequestEnvelope.class)

            expect:

            ResponseEnvelope response = new AlexaGroovyHandler().handleRequest(requestEnvelope, Mock(Context))
            assert response
            // strip out of the jdk agent stuff since that varies per machine
            String responseString = serializer.serialize(response)
            responseString.substring(62,responseString.length()) == "response\":{\"outputSpeech\":{\"type\":\"SSML\",\"ssml\":\"<speak>Welcome to the Alexa Skills Kit, you can say hello</speak>\"},\"card\":{\"type\":\"Simple\",\"title\":\"HelloWorld\",\"content\":\"Welcome to the Alexa Skills Kit, you can say hello\"},\"reprompt\":{\"outputSpeech\":{\"type\":\"SSML\",\"ssml\":\"<speak>Welcome to the Alexa Skills Kit, you can say hello</speak>\"}},\"shouldEndSession\":false}}"

        }

        static class AlexaGroovyHandler extends MicronautRequestHandler<RequestEnvelope, ResponseEnvelope> {

            @Override
            ResponseEnvelope execute(RequestEnvelope input) {

                return Skills.standard()
                        .addRequestHandlers(
                        new CancelandStopIntentHandler(),
                        new HelloWorldIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler())
                        .withSkillId("amzn1.ask.skill.cbfe084d-1ec9-4b79-83e5-8544c7181b5b")
                        .build().invoke(input)
            }

        }

}
