import com.intel.formosa.mapper.Mapper

class BootStrap {

    def init = { servletContext ->
        Mapper.startDiscoverable()
    }

    def destroy = {
    }
}
