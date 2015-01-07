class BootStrap {

    def init = { servletContext ->
    }
    def destroy = {
    }


    Class fetchedClass = Class.forName(
            "com.intel.formosa.test.Main",
            true,
            Thread.currentThread().getContextClassLoader()
    )
}
