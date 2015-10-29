SET statement_timeout = 1;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;


COPY ncdc_stations(id, latitude, longitude, elevation, state, name, gsn_flag, hcn_flag, wmo_id) FROM stdin;
10	11	12	myState	myName	myGsnFlag	myHcnFlag	myWmoId
\.


COPY ncdc_ghcn_daily(id, date, element, value, m_flag, q_flag, s_flag, time) FROM stdin;
20	2015-10-27	myElement	myValue myMflag	myQFlag	mySFlag	2015-10-27
\.




