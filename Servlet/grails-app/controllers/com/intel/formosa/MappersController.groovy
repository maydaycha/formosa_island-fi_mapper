package com.intel.formosa

import com.intel.formosa.mapper.Mapper
import grails.converters.JSON
import org.json.simple.JSONObject


class MappersController {
    Mapper mapper = null

    def index() {
        log.info("hi")
        render "hi"
    }

    def create () {
        def jsonObject = request.JSON
        render jsonObject as JSON
    }


    def save () {

        JSONObject jsonObject = (JSONObject)request.JSON

        if (mapper == null) {
            mapper = new Mapper()
        }

        log.info("call run!")
        JSONObject result = mapper.run(jsonObject.toJSONString())

//        JSONObject result = mapper.run1(jsonObject.toJSONString())

        render result as JSON
    }

    def delete () {
        def sessionId =  params.id
        sessionId = sessionId.replaceAll("_", ".")
        println "[delete] session id : " + sessionId
        if (mapper == null) {
            render "no mapper current running: " + sessionId
        } else {
            if (sessionId != null) {
                println "[delete] start stopRuleEngine"
                mapper.stopRuleEngine(sessionId);
                JSONObject result = new JSONObject();
                result.put("action", "delete");
                result.put("success", true);
                render result as JSON
            }
        }
    }

}

