class BootStrap {

    def init = { servletContext ->
    }
    def destroy = {
    }


    Class fetchedClass = Class.forName(
            "com.intel.core.Mapper",
            true,
            Thread.currentThread().getContextClassLoader()
    )
}
