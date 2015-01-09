package com.intel.formosa

import grails.converters.JSON
import org.json.simple.JSONObject
import com.intel.formosa.test.Main
class MappersController {

    HashMap threadPool = new HashMap();
    Main main = null

    def index() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread t: threadSet) {
            print "[Pool]: " + t.getId();
            if (t.getId() == 33) {
                print "[pool]: interupt 33"
                t.interrupt();
            }
        }
    }

    def create () {
        def jsonObject = request.JSON
        render jsonObject as JSON
    }


    def save () {
        JSONObject jsonObject = (JSONObject)request.JSON

        //TODO: call test
//        com.intel.formosa.test.Main test = new com.intel.formosa.test.Main()

        if (main == null) {
            main = new Main()
        }

        JSONObject result = main.run(jsonObject.toJSONString())

//        JSONObject result = new com.intel.formosa.test.Main().run1(jsonObject.toJSONString())

        print result
//        JSONObject result = test.run(jsonObject)

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
        print  sessionId
        if (main == null) {
            render "no mapper current running: " + sessionId
        } else {
            if (sessionId != null) {
                main.stopRuleEngine(sessionId);
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

