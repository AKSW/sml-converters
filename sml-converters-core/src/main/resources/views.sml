Prefix foaf:<http://xmlns.com/foaf/0.1/>
Prefix ex:<http://ex.org/>
Prefix xsd: <http://www.w3.org/2001/XMLSchema#>

Create View example As
    Construct {
        GRAPH ?grph {
            ?empl a ex:Employee . }
        GRAPH <http://ex.org/grph23> {
            ?empl foaf:name ?name . }
    }
    With
        ?empl = uri(ex:employee, ?emp_id, '/resource')
        ?name = plainLiteral(concat(?name, '23'), 'en')
        # ?name = typedLiteral(?name, xsd:string)
        ?grph = uri(ex:graph, ?dept_id)
    From
        empl
