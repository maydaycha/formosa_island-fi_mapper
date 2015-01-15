package com.intel.formosa

import grails.converters.JSON
import org.json.simple.JSONObject
import com.intel.formosa.test.Mapper
class MappersController {

    HashMap threadPool = new HashMap();
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

        //TODO: call test
//        com.intel.formosa.test.Main test = new com.intel.formosa.test.Main()

        if (mapper == null) {
            mapper = new Mapper()
        }

        JSONObject result = mapper.run(jsonObject.toJSONString())
        log.debug("call run")

//        JSONObject result = mapper.run1(jsonObject.toJSONString())

//        JSONObject result = jsonObject
//        result.session_id == null ? "1234" : result.session_id
//
//        if (result.action == "delete") {
//            MyRunnalbe myRunnalbe = threadPool.get(result.session_id);
//            myRunnalbe.setFlag(false);
//        } else {
//            MyRunnalbe myrunnable = new MyRunnalbe(true);
//            Thread t = new Thread(myrunnable)
//            t.start()
//            threadPool.put(result.session_id, myrunnable)
//        }

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








    public class MyRunnalbe implements Runnable {
        private boolean flag;
        MyRunnalbe(boolean flag) {
            this.flag = flag;
        }
        @Override
        void run() {
            while (flag) {
                print "I am thread: " + Thread.currentThread().getId()
                sleep(1000)
            }
        }

        public setFlag(boolean flag) {
            this.flag = flag;
        }
    }
}

