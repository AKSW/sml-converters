CREATE TABLE ncdc_stations (id text PRIMARY KEY UNIQUE, latitude float, longitude float, elevation float, state text, name text, gsn_flag text, hcn_flag text, wmo_id text);
CREATE TABLE ncdc_ghcn_daily (id text NOT NULL, date date NOT NULL, element text NOT NULL, value text, m_flag text, q_flag text, s_flag text, time time, PRIMARY KEY(id, date, element) );


