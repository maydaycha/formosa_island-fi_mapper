package com.intel.formosa

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

        //TODO: call mapper

        render jsonObject as JSON
    }

    def delete () {
        render "delete!"
    }
}

