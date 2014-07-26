Prefix foaf:<http://xmlns.com/foaf/0.1/>
Prefix ex:<http://ex.org/>

Create View example As
    Construct {
        ?empl a ex:Employee .
        ?empl foaf:name ?name .
    }
    With
        ?empl = uri(ex:employee, ?emp_id)
        ?name = plainLiteral(concat(?name, '23'))
    From
        empl
