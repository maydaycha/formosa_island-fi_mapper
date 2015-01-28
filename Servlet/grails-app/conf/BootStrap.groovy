import com.intel.formosa.mapper.Mapper

class BootStrap {

    def init = { servletContext ->
        Mapper.startDiscoverable()
    }
    def destroy = {
    }


//    Class fetchedClass = Class.forName(
//            "com.intel.formosa.test.Mapper",
//            true,
//            Thread.currentThread().getContextClassLoader()
//    )
}
