Prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
Prefix gwo: <http://www.xybermotive.com/GeoKnowWeatherOnt#> .


Create View ncdc_ghcn_daily_prcp As
  Construct {
    ?s
      a gwo:WeatherObservation ;
      gwo:prcp ?o
  }
  With
    ?s = uri(?id, '-', ?date)
    ?o = typedLiteral(?prcp, xsd:float)
  From
    [[SELECT id, date, (CAST(value AS DECIMAL)/10) AS prcp FROM ncdc_ghcn_daily WHERE element = 'PRCP']]


Create View ncdc_ghcn_daily_snwd As
  Construct {
    ?s
      a gwo:WeatherObservation ;
      gwo:snwd ?o
  }
  With
    ?s = uri(?id, '-', ?date)
    ?o = typedLiteral(?snwd, xsd:int)
  From
    [[SELECT id, date, value AS snwd FROM ncdc_ghcn_daily WHERE element = 'SNWD']]


Create View ncdc_ghcn_daily_tmin As
  Construct {
    ?s
      a gwo:WeatherObservation ;
      gwo:tmin ?o
  }
  With
    ?s = uri(?id, '-', ?date)
    ?o = strdt(?tmin, xsd:float)
  From
    [[SELECT id, date, (CAST(value AS DECIMAL)/10) AS tmin FROM ncdc_ghcn_daily WHERE element = 'TMIN']]


Create View ncdc_ghcn_daily_tmax As
  Construct {
    ?s
      a wo:WeatherObservation ;
      wo:tmax ?o
  }
  With
    ?s = uri(?id, '-', ?date)
    ?o = typedLiteral(?tmax, xsd:float)
  From
    [[SELECT id, date, (CAST(value AS DECIMAL)/10) AS tmax FROM ncdc_ghcn_daily WHERE element = 'TMAX']]


Create View ncdc_stations As
  Construct {
    ?s
      a gwo:WeatherStation ;
      gwo:stationId ?i ;
      rdfs:label ?l ;
      geo:alt ?a ;
      geo:long ?x ;
      geo:lat ?y ;
      gwo:hasObservation ?o
  }
  With
    ?s = uri(?id)
    ?i = plainLiteral(?id)
    ?l = plainLiteral(?name)
    ?e = plainLiteral(?elevation)
    ?x = plainLiteral(?longitude)
    ?y = plainLiteral(?latitude)
  From
    [[SELECT a.id, a.name, a.element, b.* FROM ncdc_ghcn_daily a JOIN ncdc_stations b WHERE WHERE a.element = 'TMIN' AND a.id = b.id]]


