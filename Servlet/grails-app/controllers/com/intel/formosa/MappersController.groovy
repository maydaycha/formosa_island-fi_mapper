package com.intel.formosa

import grails.converters.JSON
import org.json.simple.JSONObject

class MappersController {
//    HashMap threadPool = new HashMap();

    def index() { }

    def create () {
        def jsonObject = request.JSON
        render jsonObject as JSON
    }


    def save () {
        JSONObject jsonObject = request.JSON

        //TODO: call test
//        com.intel.formosa.test.Main test = new com.intel.formosa.test.Main()
        JSONObject result = new com.intel.formosa.test.Main().run(jsonObject)
//        JSONObject result = test.run(jsonObject)

        render result as JSON
    }

    def delete () {
        render params as JSON
    }
}

