import sorcer.util.Sorcer;


deployment(name:'Adder') {
    groups Sorcer.getLookupGroups()
    
    artifact id:'service-dl', 'org.sorcersoft.sorcer:ex6-api:11.1'
    artifact id:'service', 'org.sorcersoft.sorcer:ex6-service:11.1'


    service(name: 'Arithmetic') {
        interfaces {
            classes 'sorcer.arithmetic.provider.Adder'
            artifact ref:'service-dl'
        }
        implementation(class:'sorcer.arithmetic.provider.AdderImpl') {
            artifact ref:'service'
        }
        associations {
            ['Adder'].each { s ->
                association name:"$s", type:'requires', property:"${s.toLowerCase()}"
            }
        }
        maintain 1
    }

    ['Adder'].each { s ->
        service(name: s) {
            interfaces {
                classes "sorcer.arithmetic.provider.$s"
                artifact ref:'service-dl'
            }
            implementation(class: "sorcer.arithmetic.provider.${s}Impl") {
                artifact ref:'service'
            }
            maintain 1
        }
    }
}
