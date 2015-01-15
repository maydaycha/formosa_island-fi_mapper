class BootStrap {

    def init = { servletContext ->
    }
    def destroy = {
    }


    Class fetchedClass = Class.forName(
            "com.intel.formosa.test.Mapper",
            true,
            Thread.currentThread().getContextClassLoader()
    )
}
