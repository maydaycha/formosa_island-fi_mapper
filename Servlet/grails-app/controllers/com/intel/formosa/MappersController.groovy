package com.intel.formosa

import com.intel.core.Mapper
import grails.converters.JSON

class MappersController {

    def index() { }

    def create () {

        def jsonObject = request.JSON

        //TODO: call mapper

        render jsonObject as JSON
    }


    def save () {
        def jsonObject = request.JSON

        Mapper mapper = new Mapper();
        def jsonString = mapper.run();

        //TODO: call mapper

        render jsonString as JSON
    }

    def delete () {
        render "delete!"
    }
}

