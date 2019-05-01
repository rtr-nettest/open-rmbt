-- 2019-02-04_19-55-36 rmbt.sql
--
-- PostgreSQL database dump
--

-- Dumped from database version 10.6 (Debian 10.6-1.pgdg90+1)
-- Dumped by pg_dump version 10.6 (Debian 10.6-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- Name: pgstattuple; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgstattuple WITH SCHEMA public;


--
-- Name: EXTENSION pgstattuple; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgstattuple IS 'show tuple-level statistics';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- Name: quantile; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS quantile WITH SCHEMA public;


--
-- Name: EXTENSION quantile; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION quantile IS 'Provides quantile aggregate function.';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: cov_geo_location_assignment_type; Type: TYPE; Schema: public; Owner: rmbt
--

CREATE TYPE public.cov_geo_location_assignment_type AS (
	location public.geometry,
	accuracy numeric
);


ALTER TYPE public.cov_geo_location_assignment_type OWNER TO rmbt;

--
-- Name: mobiletech; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.mobiletech AS ENUM (
    'unknown',
    '2G',
    '3G',
    '4G',
    'mixed'
);


ALTER TYPE public.mobiletech OWNER TO postgres;

--
-- Name: qostest; Type: TYPE; Schema: public; Owner: rmbt
--

CREATE TYPE public.qostest AS ENUM (
    'website',
    'http_proxy',
    'non_transparent_proxy',
    'dns',
    'tcp',
    'udp',
    'traceroute',
    'voip',
    'traceroute_masked'
);


ALTER TYPE public.qostest OWNER TO rmbt;

--
-- Name: _final_median(anyarray); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public._final_median(anyarray) RETURNS double precision
    LANGUAGE sql IMMUTABLE
    AS $_$ 
  WITH q AS
  (
     SELECT val
     FROM unnest($1) val
     WHERE VAL IS NOT NULL
     ORDER BY 1
  ),
  cnt AS
  (
    SELECT COUNT(*) AS c FROM q
  )
  SELECT AVG(val)::float8
  FROM 
  (
    SELECT val FROM q
    LIMIT  2 - MOD((SELECT c FROM cnt), 2)
    OFFSET GREATEST(CEIL((SELECT c FROM cnt) / 2.0) - 1,0)  
  ) q2;
$_$;


ALTER FUNCTION public._final_median(anyarray) OWNER TO postgres;

--
-- Name: affine(public.geometry, double precision, double precision, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.affine(public.geometry, double precision, double precision, double precision, double precision, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $2, $3, 0,  $4, $5, 0,  0, 0, 1,  $6, $7, 0)$_$;


ALTER FUNCTION public.affine(public.geometry, double precision, double precision, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: asgml(public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.asgml(public.geometry) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsGML(2, $1, 15, 0, null, null)$_$;


ALTER FUNCTION public.asgml(public.geometry) OWNER TO postgres;

--
-- Name: asgml(public.geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.asgml(public.geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsGML(2, $1, $2, 0, null, null)$_$;


ALTER FUNCTION public.asgml(public.geometry, integer) OWNER TO postgres;

--
-- Name: askml(public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.askml(public.geometry) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML(2, ST_Transform($1,4326), 15, null)$_$;


ALTER FUNCTION public.askml(public.geometry) OWNER TO postgres;

--
-- Name: askml(public.geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.askml(public.geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML(2, ST_transform($1,4326), $2, null)$_$;


ALTER FUNCTION public.askml(public.geometry, integer) OWNER TO postgres;

--
-- Name: askml(integer, public.geometry, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.askml(integer, public.geometry, integer) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT _ST_AsKML($1, ST_Transform($2,4326), $3, null)$_$;


ALTER FUNCTION public.askml(integer, public.geometry, integer) OWNER TO postgres;

--
-- Name: bdmpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.bdmpolyfromtext(text, integer) RETURNS public.geometry
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline geometry;
	geom geometry;
BEGIN
	mline := ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := ST_Multi(ST_BuildArea(mline));

	RETURN geom;
END;
$_$;


ALTER FUNCTION public.bdmpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: bdpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.bdpolyfromtext(text, integer) RETURNS public.geometry
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	geomtext alias for $1;
	srid alias for $2;
	mline geometry;
	geom geometry;
BEGIN
	mline := ST_MultiLineStringFromText(geomtext, srid);

	IF mline IS NULL
	THEN
		RAISE EXCEPTION 'Input is not a MultiLinestring';
	END IF;

	geom := ST_BuildArea(mline);

	IF GeometryType(geom) != 'POLYGON'
	THEN
		RAISE EXCEPTION 'Input returns more then a single polygon, try using BdMPolyFromText instead';
	END IF;

	RETURN geom;
END;
$_$;


ALTER FUNCTION public.bdpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: buffer(public.geometry, double precision, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.buffer(public.geometry, double precision, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_Buffer($1, $2, $3)$_$;


ALTER FUNCTION public.buffer(public.geometry, double precision, integer) OWNER TO postgres;

--
-- Name: cov_get_donor_geo_location(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.cov_get_donor_geo_location(test_uid bigint) RETURNS public.cov_geo_location_assignment_type
    LANGUAGE sql IMMUTABLE STRICT
    AS $$
	SELECT ST_Line_Interpolate_Point(ST_MakeLine(geom_pre,geom_post), time_relative), 
		GREATEST(accuracy_post, accuracy_pre, ST_Distance(geom_post::geography, geom_pre::geography))::numeric from
		(select p1.*, p2.*, p1.dist_pre/(p1.dist_pre+p2.dist_post) as time_relative from 
			(select extract(epoch from (ct.test_start_time - cg.time)) as dist_pre, cg.location as geom_pre, cg.accuracy as accuracy_pre from coverage_test ct 
				join coverage_geo_location cg on cg.client_uuid = ct.geo_location_donor_uuid
				where cg.provider='gps' and ct.uid = test_uid and cg.time < ct.test_start_time and (ct.test_start_time - INTERVAL '6 minutes') < cg.time order by cg.time desc limit 1) as p1,
			(select extract(epoch from (cg.time - ct.test_start_time)) as dist_post, cg.location as geom_post, cg.accuracy as accuracy_post from coverage_test ct 
				join coverage_geo_location cg on cg.client_uuid = ct.geo_location_donor_uuid
				where cg.provider='gps' and ct.uid = test_uid and cg.time >= ct.test_start_time and (ct.test_start_time + INTERVAL '6 minutes') > cg.time order by cg.time asc limit 1) as p2
		) as t1;
$$;


ALTER FUNCTION public.cov_get_donor_geo_location(test_uid bigint) OWNER TO rmbt;

--
-- Name: cov_get_own_geo_location(bigint, integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.cov_get_own_geo_location(test_uid bigint, max_age integer) RETURNS public.cov_geo_location_assignment_type
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
DECLARE
	dist_pre numeric;
	accuracy_pre numeric;
	geom_pre geometry;
	dist_post numeric;
	accuracy_post numeric;
	geom_post geometry;
	geom_accuracy numeric;
BEGIN

	select extract(epoch from (ct.test_start_time - cg.time)), cg.location, cg.accuracy into dist_pre, geom_pre, accuracy_pre from coverage_test ct 
			join coverage_geo_location cg on cg.client_uuid = ct.client_uuid
			where ct.uid = test_uid and cg.time < ct.test_start_time 
				and extract(epoch from (ct.test_start_time - cg.time)) <= max_age
			order by cg.time desc limit 1;

	select extract(epoch from (cg.time - ct.test_start_time)), cg.location, cg.accuracy into dist_post, geom_post, accuracy_post from coverage_test ct 
			join coverage_geo_location cg on cg.client_uuid = ct.client_uuid
			where ct.uid = test_uid and cg.time >= ct.test_start_time 
				and extract(epoch from (cg.time - ct.test_start_time)) <= max_age
			order by cg.time asc limit 1;

	raise notice '% % %',accuracy_post, accuracy_pre, ST_Distance(geom_post::geography, geom_pre::geography);
	geom_accuracy := GREATEST(accuracy_post, accuracy_pre, ST_Distance(geom_post::geography, geom_pre::geography));

	IF (dist_pre NOTNULL OR dist_post NOTNULL) THEN
		-- raise notice '% % % % ', geom_pre, geom_post, coalesce(dist_pre,9999), coalesce(dist_post,9999);
		IF (coalesce(dist_pre, (max_age+1)) < coalesce(dist_post, (max_age+1))) THEN
			RETURN ROW(geom_pre, geom_accuracy);
		ELSE
			RETURN ROW(geom_post, geom_accuracy);
		END IF;
	ELSE
		RETURN NULL;
	END IF;
END;
$$;


ALTER FUNCTION public.cov_get_own_geo_location(test_uid bigint, max_age integer) OWNER TO rmbt;

--
-- Name: cov_get_own_geo_location_uid(bigint, integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.cov_get_own_geo_location_uid(test_uid bigint, max_age integer) RETURNS bigint
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
DECLARE
	dist_pre numeric;
	uid_pre bigint;
	dist_post numeric;
	uid_post bigint;
BEGIN

	select extract(epoch from (ct.test_start_time - cg.time)), cg.uid into dist_pre, uid_pre from coverage_test ct 
			join coverage_geo_location cg on cg.client_uuid = ct.geo_location_donor_uuid
			where ct.uid = test_uid and cg.time < ct.test_start_time 
				and extract(epoch from (ct.test_start_time - cg.time)) <= max_age
			order by cg.time desc limit 1;

	select extract(epoch from (cg.time - ct.test_start_time)), cg.uid into dist_post, uid_post from coverage_test ct 
			join coverage_geo_location cg on cg.client_uuid = ct.geo_location_donor_uuid
			where ct.uid = test_uid and cg.time >= ct.test_start_time 
				and extract(epoch from (cg.time - ct.test_start_time)) <= max_age
			order by cg.time asc limit 1;

	IF (dist_pre NOTNULL OR dist_post NOTNULL) THEN
		IF (coalesce(dist_pre, (max_age+1)) < coalesce(dist_post, (max_age+1))) THEN
			RETURN uid_pre;
		ELSE
			RETURN uid_post;
		END IF;
	ELSE
		RETURN NULL;
	END IF;
END;
$$;


ALTER FUNCTION public.cov_get_own_geo_location_uid(test_uid bigint, max_age integer) OWNER TO rmbt;

--
-- Name: cov_get_signal_strength_items(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.cov_get_signal_strength_items(test_uid bigint) RETURNS TABLE(signal_strength integer, time_ns bigint, name character varying, type character varying)
    LANGUAGE sql IMMUTABLE STRICT ROWS 100
    AS $$
	SELECT COALESCE(_j.signal->>'signal_strength', _j.signal->>'lte_rsrp',_j.signal->>'wifi_rssi')::integer AS signal_strength,
		(_j.signal->>'time_ns')::bigint AS time_ns, nt.name, nt.type FROM
			(SELECT jsonb_array_elements(ct.signal_items) AS signal FROM coverage_test ct WHERE ct.uid=test_uid) AS _j
		JOIN network_type nt ON nt.uid = (signal->>'network_id')::int;
$$;


ALTER FUNCTION public.cov_get_signal_strength_items(test_uid bigint) OWNER TO rmbt;

--
-- Name: cov_signal_json_to_csv(jsonb); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.cov_signal_json_to_csv(signals jsonb) RETURNS character varying
    LANGUAGE sql IMMUTABLE STRICT
    AS $$

SELECT string_agg(
round(((s->>'time_ns')::double precision / 1000000000)::numeric, 3)
|| ';' || (s->'lte_rsrp')
, ';')
 FROM jsonb_array_elements(signals) s;
$$;


ALTER FUNCTION public.cov_signal_json_to_csv(signals jsonb) OWNER TO rmbt;

--
-- Name: find_extent(text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.find_extent(text, text) RETURNS public.box2d
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	tablename alias for $1;
	columnname alias for $2;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT ST_Extent("' || columnname || '") As extent FROM "' || tablename || '"' LOOP
		return myrec.extent;
	END LOOP;
END;
$_$;


ALTER FUNCTION public.find_extent(text, text) OWNER TO postgres;

--
-- Name: find_extent(text, text, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.find_extent(text, text, text) RETURNS public.box2d
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$
DECLARE
	schemaname alias for $1;
	tablename alias for $2;
	columnname alias for $3;
	myrec RECORD;

BEGIN
	FOR myrec IN EXECUTE 'SELECT ST_Extent("' || columnname || '") FROM "' || schemaname || '"."' || tablename || '" As extent ' LOOP
		return myrec.extent;
	END LOOP;
END;
$_$;


ALTER FUNCTION public.find_extent(text, text, text) OWNER TO postgres;

--
-- Name: fix_geometry_columns(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fix_geometry_columns() RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	mislinked record;
	result text;
	linked integer;
	deleted integer;
	foundschema integer;
BEGIN

	-- Since 7.3 schema support has been added.
	-- Previous postgis versions used to put the database name in
	-- the schema column. This needs to be fixed, so we try to
	-- set the correct schema for each geometry_colums record
	-- looking at table, column, type and srid.
	
	return 'This function is obsolete now that geometry_columns is a view';

END;
$$;


ALTER FUNCTION public.fix_geometry_columns() OWNER TO postgres;

--
-- Name: geomcollfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomcollfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromtext(text) OWNER TO postgres;

--
-- Name: geomcollfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomcollfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromtext(text, integer) OWNER TO postgres;

--
-- Name: geomcollfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomcollfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromWKB($1)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromwkb(bytea) OWNER TO postgres;

--
-- Name: geomcollfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomcollfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromWKB($1, $2)) = 'GEOMETRYCOLLECTION'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.geomcollfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: geomfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_GeomFromText($1)$_$;


ALTER FUNCTION public.geomfromtext(text) OWNER TO postgres;

--
-- Name: geomfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_GeomFromText($1, $2)$_$;


ALTER FUNCTION public.geomfromtext(text, integer) OWNER TO postgres;

--
-- Name: geomfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.geomfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_SetSRID(ST_GeomFromWKB($1), $2)$_$;


ALTER FUNCTION public.geomfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: get_replication_delay(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_replication_delay() RETURNS SETOF pg_stat_replication
    LANGUAGE sql SECURITY DEFINER
    AS $$ SELECT * FROM pg_catalog.pg_stat_replication; $$;


ALTER FUNCTION public.get_replication_delay() OWNER TO postgres;

--
-- Name: get_sync_code(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.get_sync_code(client_uuid uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE 
	return_code VARCHAR;
	count integer;
	
BEGIN
count := 0;
SELECT sync_code INTO return_code FROM client WHERE client.uuid = CAST(client_uuid AS UUID);

if (return_code ISNULL OR char_length(return_code) < 1) then
	LOOP
		return_code := random_sync_code(7);
		BEGIN
			UPDATE client
			SET sync_code = return_code
			WHERE client.uuid = CAST(client_uuid AS UUID);
			return return_code;
		EXCEPTION WHEN unique_violation THEN
			-- return NULL when tried 10 times;
			if (count > 10) then
				return NULL;
			end if;
			count := count + 1;
		END;
	END LOOP;
else 
	return return_code;
end if;
END;
$$;


ALTER FUNCTION public.get_sync_code(client_uuid uuid) OWNER TO rmbt;

--
-- Name: hstore2json(public.hstore); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.hstore2json(hs public.hstore) RETURNS text
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
rv text;
r record;
BEGIN
rv:='';
for r in (select key, val from each(hs) as h(key, val)) loop
if rv<>'' then
rv:=rv||',';
end if;
rv:=rv || '"' || r.key || '":';
r.val := REPLACE(r.val, E'\\', E'\\\\');
r.val := REPLACE(r.val, '"', E'\\"');
r.val := REPLACE(r.val, E'\n', E'\\n');
r.val := REPLACE(r.val, E'\r', E'\\r');
rv:=rv || CASE WHEN r.val IS NULL THEN 'null' ELSE '"' || r.val || '"' END;
end loop;
return '{'||rv||'}';
END;
$$;


ALTER FUNCTION public.hstore2json(hs public.hstore) OWNER TO postgres;

--
-- Name: jsonb_array_map(jsonb, text[]); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.jsonb_array_map(json_arr jsonb, path text[]) RETURNS jsonb[]
    LANGUAGE plpgsql IMMUTABLE
    AS $$
DECLARE
    rec jsonb;
    len int;
    ret jsonb[];
BEGIN
    -- If json_arr is not an array, return an empty array as the result
    BEGIN
        len := jsonb_array_length(json_arr);
    EXCEPTION
        WHEN OTHERS THEN
            RETURN ret;
    END;

    -- Apply mapping in a loop
    FOR rec IN SELECT jsonb_array_elements#>path FROM jsonb_array_elements(json_arr)  
    LOOP
	--RAISE NOTICE 'get for %', path;
        ret := array_append(ret,rec);
    END LOOP;
    RETURN ret;
END $$;


ALTER FUNCTION public.jsonb_array_map(json_arr jsonb, path text[]) OWNER TO rmbt;

--
-- Name: linefromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linefromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'LINESTRING'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromtext(text) OWNER TO postgres;

--
-- Name: linefromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linefromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'LINESTRING'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromtext(text, integer) OWNER TO postgres;

--
-- Name: linefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linefromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'LINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromwkb(bytea) OWNER TO postgres;

--
-- Name: linefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linefromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: linestringfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linestringfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT LineFromText($1)$_$;


ALTER FUNCTION public.linestringfromtext(text) OWNER TO postgres;

--
-- Name: linestringfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linestringfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT LineFromText($1, $2)$_$;


ALTER FUNCTION public.linestringfromtext(text, integer) OWNER TO postgres;

--
-- Name: linestringfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linestringfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'LINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linestringfromwkb(bytea) OWNER TO postgres;

--
-- Name: linestringfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.linestringfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'LINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.linestringfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: locate_along_measure(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.locate_along_measure(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT ST_locate_between_measures($1, $2, $2) $_$;


ALTER FUNCTION public.locate_along_measure(public.geometry, double precision) OWNER TO postgres;

--
-- Name: mlinefromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mlinefromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTILINESTRING'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromtext(text) OWNER TO postgres;

--
-- Name: mlinefromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mlinefromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE
	WHEN geometrytype(GeomFromText($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromtext(text, integer) OWNER TO postgres;

--
-- Name: mlinefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mlinefromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromwkb(bytea) OWNER TO postgres;

--
-- Name: mlinefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mlinefromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mlinefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: mpointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpointfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTIPOINT'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromtext(text) OWNER TO postgres;

--
-- Name: mpointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpointfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1,$2)) = 'MULTIPOINT'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromtext(text, integer) OWNER TO postgres;

--
-- Name: mpointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpointfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromwkb(bytea) OWNER TO postgres;

--
-- Name: mpointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpointfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'MULTIPOINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: mpolyfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpolyfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'MULTIPOLYGON'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromtext(text) OWNER TO postgres;

--
-- Name: mpolyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpolyfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromtext(text, integer) OWNER TO postgres;

--
-- Name: mpolyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpolyfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromwkb(bytea) OWNER TO postgres;

--
-- Name: mpolyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.mpolyfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.mpolyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multilinefromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multilinefromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multilinefromwkb(bytea) OWNER TO postgres;

--
-- Name: multilinefromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multilinefromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTILINESTRING'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multilinefromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multilinestringfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multilinestringfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_MLineFromText($1)$_$;


ALTER FUNCTION public.multilinestringfromtext(text) OWNER TO postgres;

--
-- Name: multilinestringfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multilinestringfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MLineFromText($1, $2)$_$;


ALTER FUNCTION public.multilinestringfromtext(text, integer) OWNER TO postgres;

--
-- Name: multipointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipointfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPointFromText($1)$_$;


ALTER FUNCTION public.multipointfromtext(text) OWNER TO postgres;

--
-- Name: multipointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipointfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPointFromText($1, $2)$_$;


ALTER FUNCTION public.multipointfromtext(text, integer) OWNER TO postgres;

--
-- Name: multipointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipointfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipointfromwkb(bytea) OWNER TO postgres;

--
-- Name: multipointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipointfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'MULTIPOINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multipolyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipolyfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipolyfromwkb(bytea) OWNER TO postgres;

--
-- Name: multipolyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipolyfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'MULTIPOLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.multipolyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: multipolygonfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipolygonfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPolyFromText($1)$_$;


ALTER FUNCTION public.multipolygonfromtext(text) OWNER TO postgres;

--
-- Name: multipolygonfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.multipolygonfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT MPolyFromText($1, $2)$_$;


ALTER FUNCTION public.multipolygonfromtext(text, integer) OWNER TO postgres;

--
-- Name: pointfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.pointfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'POINT'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromtext(text) OWNER TO postgres;

--
-- Name: pointfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.pointfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'POINT'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromtext(text, integer) OWNER TO postgres;

--
-- Name: pointfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.pointfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POINT'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromwkb(bytea) OWNER TO postgres;

--
-- Name: pointfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.pointfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(ST_GeomFromWKB($1, $2)) = 'POINT'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.pointfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: polyfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polyfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1)) = 'POLYGON'
	THEN GeomFromText($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromtext(text) OWNER TO postgres;

--
-- Name: polyfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polyfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromText($1, $2)) = 'POLYGON'
	THEN GeomFromText($1,$2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromtext(text, integer) OWNER TO postgres;

--
-- Name: polyfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polyfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromwkb(bytea) OWNER TO postgres;

--
-- Name: polyfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polyfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1, $2)) = 'POLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polyfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: polygonfromtext(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polygonfromtext(text) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT PolyFromText($1)$_$;


ALTER FUNCTION public.polygonfromtext(text) OWNER TO postgres;

--
-- Name: polygonfromtext(text, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polygonfromtext(text, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT PolyFromText($1, $2)$_$;


ALTER FUNCTION public.polygonfromtext(text, integer) OWNER TO postgres;

--
-- Name: polygonfromwkb(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polygonfromwkb(bytea) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1)) = 'POLYGON'
	THEN GeomFromWKB($1)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polygonfromwkb(bytea) OWNER TO postgres;

--
-- Name: polygonfromwkb(bytea, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.polygonfromwkb(bytea, integer) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
	SELECT CASE WHEN geometrytype(GeomFromWKB($1,$2)) = 'POLYGON'
	THEN GeomFromWKB($1, $2)
	ELSE NULL END
	$_$;


ALTER FUNCTION public.polygonfromwkb(bytea, integer) OWNER TO postgres;

--
-- Name: probe_geometry_columns(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.probe_geometry_columns() RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
	inserted integer;
	oldcount integer;
	probed integer;
	stale integer;
BEGIN




	RETURN 'This function is obsolete now that geometry_columns is a view';
END

$$;


ALTER FUNCTION public.probe_geometry_columns() OWNER TO postgres;

--
-- Name: random_sync_code(integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.random_sync_code(integer) RETURNS text
    LANGUAGE sql
    AS $_$

    select upper(
        substring(
            (
                SELECT string_agg(md5(random()::TEXT), '')
                FROM generate_series(1, CEIL($1 / 32.)::integer)
                ),
        (33-$1))
    );

$_$;


ALTER FUNCTION public.random_sync_code(integer) OWNER TO rmbt;

--
-- Name: rename_geometry_table_constraints(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rename_geometry_table_constraints() RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $$
SELECT 'rename_geometry_table_constraint() is obsoleted'::text
$$;


ALTER FUNCTION public.rename_geometry_table_constraints() OWNER TO postgres;

--
-- Name: rmbt_fill_open_uuid(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.rmbt_fill_open_uuid() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
 _t RECORD;
 _uuid uuid;
BEGIN

FOR _t IN SELECT uid,client_id,time FROM test WHERE open_uuid IS NULL ORDER BY uid LOOP
    SELECT INTO _uuid open_uuid FROM test WHERE client_id=_t.client_id AND (_t.time - INTERVAL '4 hours' < time) AND uid<_t.uid ORDER BY uid DESC LIMIT 1;
    IF (_uuid IS NULL) THEN
        _uuid = uuid_generate_v4();
    END IF;
    UPDATE test SET open_uuid=_uuid WHERE uid=_t.uid;
END LOOP;

END;$$;


ALTER FUNCTION public.rmbt_fill_open_uuid() OWNER TO rmbt;

--
-- Name: rmbt_get_next_test_slot(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.rmbt_get_next_test_slot(_test_id bigint) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  _slot integer;
  _count integer;
  _server_id integer;
BEGIN
SELECT server_id FROM test WHERE uid = _test_id INTO _server_id;
_slot := EXTRACT(EPOCH FROM NOW())::int - 2;
_count := 100;
WHILE _count >= 5 LOOP
  _slot := _slot + 1;
  SELECT COUNT(uid) FROM test WHERE test_slot = _slot AND server_id=_server_id INTO _count;
END LOOP;
  UPDATE test SET test_slot = _slot WHERE uid = _test_id;
RETURN _slot;
END;
$$;


ALTER FUNCTION public.rmbt_get_next_test_slot(_test_id bigint) OWNER TO rmbt;

--
-- Name: rmbt_get_sync_code(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.rmbt_get_sync_code(client_uuid uuid) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE 
	return_code VARCHAR;
	count integer;
	
BEGIN
count := 0;
SELECT sync_code INTO return_code FROM client WHERE client.uuid = CAST(client_uuid AS UUID) AND sync_code_timestamp + INTERVAL '1 month' > NOW();

if (return_code ISNULL OR char_length(return_code) < 1) then
	LOOP
		return_code := random_sync_code(12);
		BEGIN
			UPDATE client
			SET sync_code = return_code,
			sync_code_timestamp = NOW()
			WHERE client.uuid = CAST(client_uuid AS UUID);
			return return_code;
		EXCEPTION WHEN unique_violation THEN
			-- return NULL when tried 10 times;
			if (count > 10) then
				return NULL;
			end if;
			count := count + 1;
		END;
	END LOOP;
else 
	return return_code;
end if;
END;
$$;


ALTER FUNCTION public.rmbt_get_sync_code(client_uuid uuid) OWNER TO rmbt;

--
-- Name: rmbt_random_sync_code(integer); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.rmbt_random_sync_code(integer) RETURNS text
    LANGUAGE sql
    AS $_$

    select upper(
        substring(
            (
                SELECT string_agg(md5(random()::TEXT), '')
                FROM generate_series(1, CEIL($1 / 32.)::integer)
                ),
        (33-$1))
    );

$_$;


ALTER FUNCTION public.rmbt_random_sync_code(integer) OWNER TO rmbt;

--
-- Name: rmbt_set_provider_from_as(bigint); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.rmbt_set_provider_from_as(_test_id bigint) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
  _asn bigint;
  _rdns character varying;
  _provider_id integer;
  _provider_name character varying;
BEGIN

SELECT
  ap.provider_id,
  p.shortname
  FROM test t
  JOIN as2provider ap
  ON t.public_ip_asn=ap.asn 
  AND (ap.dns_part IS NULL OR t.public_ip_rdns ILIKE ap.dns_part /*Case insensitive regexp, DJ per #235:*/ OR t.public_ip_rdns ~* ap.dns_part)
  JOIN provider p
  ON p.uid = ap.provider_id
  WHERE t.uid = _test_id
  ORDER BY dns_part IS NOT NULL DESC
  LIMIT 1
  INTO _provider_id, _provider_name;

IF _provider_id IS NOT NULL THEN
  UPDATE test SET provider_id = _provider_id WHERE uid = _test_id;
  RETURN _provider_name;
ELSE
  RETURN NULL;
END IF;

END;
$$;


ALTER FUNCTION public.rmbt_set_provider_from_as(_test_id bigint) OWNER TO rmbt;

--
-- Name: rotate(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rotate(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_rotateZ($1, $2)$_$;


ALTER FUNCTION public.rotate(public.geometry, double precision) OWNER TO postgres;

--
-- Name: rotatex(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rotatex(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1, 1, 0, 0, 0, cos($2), -sin($2), 0, sin($2), cos($2), 0, 0, 0)$_$;


ALTER FUNCTION public.rotatex(public.geometry, double precision) OWNER TO postgres;

--
-- Name: rotatey(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rotatey(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  cos($2), 0, sin($2),  0, 1, 0,  -sin($2), 0, cos($2), 0,  0, 0)$_$;


ALTER FUNCTION public.rotatey(public.geometry, double precision) OWNER TO postgres;

--
-- Name: rotatez(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rotatez(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  cos($2), -sin($2), 0,  sin($2), cos($2), 0,  0, 0, 1,  0, 0, 0)$_$;


ALTER FUNCTION public.rotatez(public.geometry, double precision) OWNER TO postgres;

--
-- Name: scale(public.geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.scale(public.geometry, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_scale($1, $2, $3, 1)$_$;


ALTER FUNCTION public.scale(public.geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: scale(public.geometry, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.scale(public.geometry, double precision, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $2, 0, 0,  0, $3, 0,  0, 0, $4,  0, 0, 0)$_$;


ALTER FUNCTION public.scale(public.geometry, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: se_envelopesintersect(public.geometry, public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.se_envelopesintersect(public.geometry, public.geometry) RETURNS boolean
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ 
	SELECT $1 && $2
	$_$;


ALTER FUNCTION public.se_envelopesintersect(public.geometry, public.geometry) OWNER TO postgres;

--
-- Name: se_locatealong(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.se_locatealong(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT SE_LocateBetween($1, $2, $2) $_$;


ALTER FUNCTION public.se_locatealong(public.geometry, double precision) OWNER TO postgres;

--
-- Name: snaptogrid(public.geometry, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.snaptogrid(public.geometry, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_SnapToGrid($1, 0, 0, $2, $2)$_$;


ALTER FUNCTION public.snaptogrid(public.geometry, double precision) OWNER TO postgres;

--
-- Name: st_astext(bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.st_astext(bytea) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$ SELECT ST_AsText($1::geometry);$_$;


ALTER FUNCTION public.st_astext(bytea) OWNER TO postgres;

--
-- Name: translate(public.geometry, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.translate(public.geometry, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_translate($1, $2, $3, 0)$_$;


ALTER FUNCTION public.translate(public.geometry, double precision, double precision) OWNER TO postgres;

--
-- Name: translate(public.geometry, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.translate(public.geometry, double precision, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1, 1, 0, 0, 0, 1, 0, 0, 0, 1, $2, $3, $4)$_$;


ALTER FUNCTION public.translate(public.geometry, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: transscale(public.geometry, double precision, double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.transscale(public.geometry, double precision, double precision, double precision, double precision) RETURNS public.geometry
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT st_affine($1,  $4, 0, 0,  0, $5, 0,
		0, 0, 1,  $2 * $4, $3 * $5, 0)$_$;


ALTER FUNCTION public.transscale(public.geometry, double precision, double precision, double precision, double precision) OWNER TO postgres;

--
-- Name: trigger_test(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.trigger_test() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    _tmp_uuid uuid;
    _tmp_uid integer;
    _tmp_time timestamp;
    _tmp_network_group_name VARCHAR;
    _mcc_sim VARCHAR;
    _mcc_net VARCHAR;
    _min_accuracy CONSTANT integer := 3000;
    _tmp_location geometry;

    v_old_data TEXT;
    v_new_data TEXT;

BEGIN

    IF ((TG_OP = 'INSERT' OR NEW.speed_download IS DISTINCT FROM OLD.speed_download) AND NEW.speed_download > 0) THEN
        NEW.speed_download_log=(log(NEW.speed_download::double precision/10))/4;
    END IF;
    IF ((TG_OP = 'INSERT' OR NEW.speed_upload IS DISTINCT FROM OLD.speed_upload) AND NEW.speed_upload > 0) THEN
        NEW.speed_upload_log=(log(NEW.speed_upload::double precision/10))/4;
    END IF;
    IF ((TG_OP = 'INSERT' OR NEW.ping_shortest IS DISTINCT FROM OLD.ping_shortest) AND NEW.ping_shortest > 0) THEN
        NEW.ping_shortest_log=(log(NEW.ping_shortest::double precision/1000000))/3;
        SELECT INTO NEW.ping_median floor(median(coalesce(value_server,value))) FROM ping WHERE NEW.uid = test_id;
        NEW.ping_median_log=(log(NEW.ping_median::double precision/1000000))/3;
        IF (NEW.ping_median IS NULL) THEN
             NEW.ping_median = NEW.ping_shortest;
        END IF;
    END IF;

    IF (TG_OP = 'INSERT' OR NEW.location IS DISTINCT FROM OLD.location) THEN
        IF (NEW.location IS NULL OR NEW.geo_accuracy > _min_accuracy) THEN
            NEW.country_location = NULL;
            NEW.gkz = NULL;
        ELSE
            -- add Austrian Gemeinde Kennzeichen (community identifier)
            SELECT INTO NEW.gkz gemeinde_i
            FROM kategorisierte_gemeinden
            WHERE NEW.location && the_geom AND Within(NEW.location, the_geom)
            LIMIT 1;


            -- add BEV gkz (community identifier) and kg_nr (settlement identifier)
            SELECT bev.gkz::INTEGER, bev.kg_nr::INTEGER
            INTO NEW.gkz_bev, NEW.kg_nr_bev
            FROM oesterreich_bev_kg_lam_mitattribute_2017_10_02 bev
            WHERE within(st_transform(NEW.location, 31287), bev.geom)
            LIMIT 1;

            -- add SA gkz (community identifier)
            SELECT sa.id::INTEGER
            INTO NEW.gkz_sa
            FROM statistik_austria_gem_20180101 sa
            WHERE within(st_transform(NEW.location, 31287), sa.geom)
            LIMIT 1;

            -- add land cover id
            SELECT clc.code_12::INTEGER
            INTO NEW.land_cover
            FROM clc12_all_oesterreich clc
            WHERE within(st_transform(NEW.location, 3035), clc.geom)
            LIMIT 1;


            IF (NEW.gkz_bev IS NOT NULL) THEN -- #659: kategorisierte_gemeinden is more accurate/up-to-date for AT than ne_50_admin_0_countries or plz2001
                NEW.country_location = 'AT';
            ELSE
                SELECT INTO NEW.country_location iso_a2
                FROM ne_10m_admin_0_countries
                WHERE NEW.location && geom AND Within(NEW.location, geom) AND char_length(iso_a2)=2
                AND iso_a2 IS DISTINCT FROM 'AT' -- #659: because ne_50_admin_0_countries is inaccurate, do not allow to return 'AT'
                LIMIT 1;
            END IF;
        END IF;
    END IF;

    IF (TG_OP = 'INSERT'
        OR NEW.network_sim_operator IS DISTINCT FROM OLD.network_sim_operator
        OR NEW.network_operator IS DISTINCT FROM OLD.network_operator
        OR NEW.time IS DISTINCT FROM OLD.time
        ) THEN

            IF (NEW.network_sim_operator IS NULL OR NEW.network_operator IS NULL) THEN
                NEW.roaming_type = NULL;
            ELSE
                IF (NEW.network_sim_operator = NEW.network_operator) THEN
                    NEW.roaming_type = 0; -- no roaming
                ELSE
                    _mcc_sim := split_part(NEW.network_sim_operator, '-', 1);
                    _mcc_net := split_part(NEW.network_operator, '-', 1);
                    IF (_mcc_sim = _mcc_net) THEN
                        NEW.roaming_type = 1;  -- national roaming
                    ELSE
                        NEW.roaming_type = 2;  -- international roaming
                    END IF;
                END IF;
            END IF;

            IF ((NEW.roaming_type IS NULL AND NEW.country_location IS DISTINCT FROM 'AT') OR NEW.roaming_type IS NOT DISTINCT FROM 2) THEN -- not for foreign networks #659 bug correction
                NEW.mobile_provider_id = NULL;
            ELSE
                SELECT INTO NEW.mobile_provider_id provider_id FROM mccmnc2provider
                    WHERE mcc_mnc_sim = NEW.network_sim_operator
                    AND (valid_from IS NULL OR valid_from <= NEW.time) AND (valid_to IS NULL OR valid_to >= NEW.time)
                    AND (mcc_mnc_network IS NULL OR mcc_mnc_network = NEW.network_operator)
                    ORDER BY mcc_mnc_network NULLS LAST
                    LIMIT 1;
            END IF;
    END IF;

     IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED')
          AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN -- update only new entries, skip old entries
          IF (NEW.network_operator is not NULL) THEN
            SELECT INTO NEW.mobile_network_id COALESCE(n.mapped_uid,n.uid)
                FROM mccmnc2name n
                WHERE NEW.network_operator=n.mccmnc
                AND (n.valid_from is null OR n.valid_from <= NEW.time)
                AND (n.valid_to is null or n.valid_to  >= NEW.time)
                AND use_for_network=TRUE
                ORDER BY n.uid NULLS LAST
                LIMIT 1;
          END IF;

          IF (NEW.network_sim_operator is not NULL) THEN
          SELECT INTO NEW.mobile_sim_id COALESCE(n.mapped_uid,n.uid)
                FROM mccmnc2name n
                WHERE NEW.network_sim_operator=n.mccmnc
                AND (n.valid_from is null OR n.valid_from <= NEW.time)
                AND (n.valid_to is null or n.valid_to  >= NEW.time)
                AND (NEW.network_sim_operator=n.mcc_mnc_network_mapping OR n.mcc_mnc_network_mapping is NULL)
                AND use_for_sim=TRUE
                ORDER BY n.uid NULLS LAST
                LIMIT 1;
          END IF;

     END IF;


    IF ((TG_OP = 'UPDATE')  AND (NEW.time > (now() - INTERVAL '5 minutes')) AND NEW.network_type=97/*CLI*/ AND NEW.deleted=FALSE) THEN
        NEW.deleted=TRUE;
        NEW.comment='Exclude CLI per #211';
    END IF;

    IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED')
      AND (NEW.time > (now() - INTERVAL '5 minutes'))
      AND NEW.model='SM-N9005'
      AND NEW.geo_provider='network') THEN
         NEW.geo_accuracy = 99999;
    END IF;

    IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED')
      AND (NEW.time > (now() - INTERVAL '5 minutes'))
      AND NEW.geo_accuracy is not null
      AND NEW.geo_accuracy <= 10000 ) THEN

     SELECT INTO _tmp_uid uid FROM test
        WHERE client_id=NEW.client_id
        AND time < NEW.time -- #668 allow only past tests
        AND (NEW.time - INTERVAL '24 hours' < time)
        AND geo_accuracy is not null
        AND geo_accuracy <= 10000
        ORDER BY uid DESC LIMIT 1;

      IF _tmp_uid is not null THEN
        SELECT INTO NEW.dist_prev ST_Distance(ST_Transform(t.location,4326)::geography,ST_Transform(NEW.location,4326)::geography) -- #668 improve geo precision for the calculation of the distance (in meters) to a previous test
        FROM test t WHERE uid=_tmp_uid;
        IF NEW.dist_prev is not null THEN
            SELECT INTO _tmp_time time FROM test t
            WHERE uid=_tmp_uid;
            NEW.speed_prev = NEW.dist_prev/GREATEST(0.000001, EXTRACT(EPOCH FROM (NEW.time - _tmp_time)))*3.6; -- #668 speed in km/h and don't allow division by zero
        END IF;
      END IF;
    END IF;

    IF ((NEW.network_type > 0) AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN
       SELECT INTO NEW.network_group_name group_name FROM network_type
          WHERE uid = NEW.network_type
          LIMIT 1;
       SELECT INTO NEW.network_group_type type FROM network_type
          WHERE uid = NEW.network_type
          LIMIT 1;
    END IF;

    -- #759 Finalisation Loop Modus
    IF (TG_OP = 'UPDATE' AND OLD.status='STARTED' AND NEW.status='FINISHED')
        AND (NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
    THEN
        _tmp_uuid = NULL;
        _tmp_location = NULL;
        SELECT open_uuid, location INTO _tmp_uuid, _tmp_location FROM test   -- find the open_uuid and location
        WHERE (NEW.client_id=client_id)             -- of the current client
        AND (NEW.time > time)                       -- thereby skipping the current entry (was: OLD.uid != uid)
        AND status = 'FINISHED'                     -- of successfull tests
        AND (NEW.time - time) < '4 hours'::INTERVAL -- within last 4 hours
        AND (NEW.time::DATE = time::DATE)           -- on the same day
        AND (NEW.network_group_type IS NOT DISTINCT FROM network_group_type) -- of the same technology (i.e. MOBILE, WLAN, LAN, CLI, NULL) - was: network_group_name
        AND (NEW.public_ip_asn IS NOT DISTINCT FROM public_ip_asn)           -- and of the same operator (including NULL)
        ORDER BY time DESC LIMIT 1;                 -- get only the latest test

        IF
            (_tmp_uuid IS NULL)                     -- previous query doesn't return any test
             OR                                     -- OR
            (NEW.location IS NOT NULL AND _tmp_location IS NOT NULL
             AND ST_Distance(ST_Transform (NEW.location, 4326),
                             ST_Transform (_tmp_location, 4326)::geography)
                 >= 100)                            -- the distance to the last test >= 100m
        THEN
            _tmp_uuid = uuid_generate_v4();         --generate new open_uuid
        END IF;
        NEW.open_uuid = _tmp_uuid;
    END IF;

    IF (TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED') THEN
        NEW.timestamp = now();

        SELECT INTO NEW.location_max_distance
          round(ST_Distance( -- #668 improve geo precision for the calculation of the diagonal length (in meters) of the bounding box of one test
           ST_SetSRID(ST_MakePoint(ST_XMin(ST_Extent(ST_Transform(location,4326))),ST_YMin(ST_Extent(ST_Transform(location,4326)))),4326)::geography,
           ST_SetSRID(ST_MakePoint(ST_XMax(ST_Extent(ST_Transform(location,4326))),ST_YMax(ST_Extent(ST_Transform(location,4326)))),4326)::geography)
          )
          FROM geo_location
          WHERE test_id=NEW.uid;
    END IF;

    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
       AND (
           (NEW.network_operator ILIKE '232%') -- test with Austrian mobile network operator
           )
       AND ST_Distance(
             ST_Transform (NEW.location, 4326), -- location of the test
             ST_Transform ((select geom from ne_10m_admin_0_countries where sovereignt ilike 'Austria'),4326)::geography -- Austria shape
           ) > 35000 -- location is more than 35 km outside of the Austria shape
    ) -- if
    THEN NEW.status='UPDATE ERROR'; NEW.comment='Automatic update error due to invalid location per #272';
    END IF;

    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
       AND NEW.network_type in (97, 98, 99, 106, 107) -- CLI, LAN, WLAN, Ethernet, Bluetooth
       AND (
           (NEW.provider_id IS NOT NULL) -- Austrian operator
           )
       AND ST_Distance(
             ST_Transform (NEW.location, 4326), -- location of the test
             ST_Transform ((select geom from ne_10m_admin_0_countries where sovereignt ilike 'Austria'),4326)::geography -- Austria shape
           ) > 3000 -- location is outside of the Austria shape with a tolerance of +3 km
    ) -- if
    THEN NEW.provider_id=NULL; NEW.comment=concat('No provider_id outside of Austria for e.g. VPNs, HotSpots, manual location/geocoder etc. per #664; ', NEW.comment, NULLIF(OLD.comment, NEW.comment));
    END IF;

    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
       AND (NEW.model='unknown') -- model is 'unknown'
       )
    THEN NEW.status='UPDATE ERROR'; NEW.comment='Automatic update error due to unknown model per #356';
    END IF;

    IF ((TG_OP = 'UPDATE' AND OLD.STATUS='STARTED' AND NEW.STATUS='FINISHED')
       AND (NEW.time > (now() - INTERVAL '5 minutes'))) -- update only new entries, skip old entries
    THEN -- Returns the uid of a previous similar test, otherwise -1. Also IF similar_test_uid = -1 then pinned = TRUE ELSE pinned=FALSE. Column similar_test_uid has a default value NULL, meaning the evaluation for similar test(s) wasn't performed yet.
       SELECT INTO NEW.similar_test_uid
          uid FROM test
          WHERE (similar_test_uid = -1 OR similar_test_uid IS NULL)       -- consider only unsimilar or not yet evaluated tests
          AND NEW.open_uuid = open_uuid                                   -- with the same open_uuid
          AND NEW.time > time AND (NEW.time - time) < '4 hours'::INTERVAL -- in the last 4 hours
          AND NEW.public_ip_asn = public_ip_asn                           -- from the same network based on AS
          AND NEW.network_type = network_type                             -- of the same network_type
          AND CASE
                WHEN (NEW.location IS NOT NULL AND NEW.geo_accuracy IS NOT NULL AND NEW.geo_accuracy < 2000
                     AND  location IS NOT NULL AND     geo_accuracy IS NOT NULL AND     geo_accuracy < 2000)
                THEN ST_Distance(
                       ST_Transform (NEW.location, 4326),
                       ST_Transform (location,4326)::geography
                     ) < GREATEST (100, NEW.geo_accuracy)                 -- either within a radius of 100 m
                ELSE TRUE                                                 -- or if no or inaccurate location, only other criteria count
              END
          ORDER BY time DESC                                              -- consider the last, most previous test
          LIMIT 1;
       IF NEW.similar_test_uid IS NULL -- no similar test found
       then NEW.similar_test_uid = -1; -- indicate that we have searched for a similar test but nothing found
            NEW.pinned = TRUE;         -- and set the pinned for the statistics
       ELSE NEW.pinned = FALSE;        -- else in similar_test_uid the uid of a previous test is stored so the test shouldn't go into the statistics
       END IF;
    END IF;

    RETURN NEW;



END;
$$;


ALTER FUNCTION public.trigger_test() OWNER TO rmbt;

--
-- Name: within(public.geometry, public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.within(public.geometry, public.geometry) RETURNS boolean
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_Within($1, $2)$_$;


ALTER FUNCTION public.within(public.geometry, public.geometry) OWNER TO postgres;

--
-- Name: accum(public.geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.accum(public.geometry) (
    SFUNC = public.pgis_geometry_accum_transfn,
    STYPE = public.pgis_abs,
    FINALFUNC = public.pgis_geometry_accum_finalfn
);


ALTER AGGREGATE public.accum(public.geometry) OWNER TO postgres;

--
-- Name: extent(public.geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.extent(public.geometry) (
    SFUNC = public.st_combine_bbox,
    STYPE = public.box3d,
    FINALFUNC = public.box2d
);


ALTER AGGREGATE public.extent(public.geometry) OWNER TO postgres;

--
-- Name: makeline(public.geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.makeline(public.geometry) (
    SFUNC = public.pgis_geometry_accum_transfn,
    STYPE = public.pgis_abs,
    FINALFUNC = public.pgis_geometry_makeline_finalfn
);


ALTER AGGREGATE public.makeline(public.geometry) OWNER TO postgres;

--
-- Name: median(anyelement); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.median(anyelement) (
    SFUNC = array_append,
    STYPE = anyarray,
    INITCOND = '{}',
    FINALFUNC = public._final_median
);


ALTER AGGREGATE public.median(anyelement) OWNER TO postgres;

--
-- Name: memcollect(public.geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.memcollect(public.geometry) (
    SFUNC = public.st_collect,
    STYPE = public.geometry
);


ALTER AGGREGATE public.memcollect(public.geometry) OWNER TO postgres;

--
-- Name: st_extent3d(public.geometry); Type: AGGREGATE; Schema: public; Owner: postgres
--

CREATE AGGREGATE public.st_extent3d(public.geometry) (
    SFUNC = public.st_combine_bbox,
    STYPE = public.box3d
);


ALTER AGGREGATE public.st_extent3d(public.geometry) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: device_map; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.device_map (
    uid integer NOT NULL,
    codename character varying(200),
    fullname character varying(200),
    source character varying(200),
    "timestamp" timestamp with time zone
);


ALTER TABLE public.device_map OWNER TO rmbt;

--
-- Name: android_device_map_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.android_device_map_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.android_device_map_uid_seq OWNER TO rmbt;

--
-- Name: android_device_map_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.android_device_map_uid_seq OWNED BY public.device_map.uid;


--
-- Name: as2provider; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.as2provider (
    uid integer NOT NULL,
    asn bigint,
    dns_part character varying(200),
    provider_id integer
);


ALTER TABLE public.as2provider OWNER TO rmbt;

--
-- Name: as2provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.as2provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.as2provider_uid_seq OWNER TO rmbt;

--
-- Name: as2provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.as2provider_uid_seq OWNED BY public.as2provider.uid;


--
-- Name: cell_location; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.cell_location (
    uid bigint NOT NULL,
    test_id bigint,
    location_id integer,
    area_code integer,
    "time" timestamp with time zone,
    primary_scrambling_code integer,
    time_ns bigint,
    open_test_uuid uuid
);


ALTER TABLE public.cell_location OWNER TO rmbt;

--
-- Name: COLUMN cell_location.open_test_uuid; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.cell_location.open_test_uuid IS 'open uuid of the test';


--
-- Name: cell_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.cell_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cell_location_uid_seq OWNER TO rmbt;

--
-- Name: cell_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.cell_location_uid_seq OWNED BY public.cell_location.uid;


--
-- Name: clc12_all_oesterreich; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.clc12_all_oesterreich (
    gid integer NOT NULL,
    code_12 character varying(3),
    id character varying(18),
    remark character varying(20),
    area_ha numeric,
    shape_leng numeric,
    shape_area numeric,
    geom public.geometry(MultiPolygonZM,3035)
);


ALTER TABLE public.clc12_all_oesterreich OWNER TO rmbt;

--
-- Name: clc12_all_oesterreich_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.clc12_all_oesterreich_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.clc12_all_oesterreich_gid_seq OWNER TO rmbt;

--
-- Name: clc12_all_oesterreich_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.clc12_all_oesterreich_gid_seq OWNED BY public.clc12_all_oesterreich.gid;


--
-- Name: clc_legend; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.clc_legend (
    grid_code integer,
    clc_code integer,
    label1 character varying,
    label2 character varying,
    label3 character varying,
    rgb character varying
);


ALTER TABLE public.clc_legend OWNER TO rmbt;

--
-- Name: client; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.client (
    uid bigint NOT NULL,
    uuid uuid NOT NULL,
    client_type_id integer,
    "time" timestamp with time zone,
    sync_group_id integer,
    sync_code character varying(12),
    terms_and_conditions_accepted boolean DEFAULT false NOT NULL,
    sync_code_timestamp timestamp with time zone,
    blacklisted boolean DEFAULT false NOT NULL,
    terms_and_conditions_accepted_version integer,
    last_seen timestamp with time zone,
    terms_and_conditions_accepted_timestamp timestamp with time zone
);


ALTER TABLE public.client OWNER TO rmbt;

--
-- Name: client_type; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.client_type (
    uid integer NOT NULL,
    name character varying(200)
);


ALTER TABLE public.client_type OWNER TO rmbt;

--
-- Name: client_type_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.client_type_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.client_type_uid_seq OWNER TO rmbt;

--
-- Name: client_type_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.client_type_uid_seq OWNED BY public.client_type.uid;


--
-- Name: client_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.client_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.client_uid_seq OWNER TO rmbt;

--
-- Name: client_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.client_uid_seq OWNED BY public.client.uid;


--
-- Name: geo_location; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.geo_location (
    uid bigint NOT NULL,
    test_id bigint NOT NULL,
    "time" timestamp with time zone,
    accuracy double precision,
    altitude double precision,
    bearing double precision,
    speed double precision,
    provider character varying(200),
    geo_lat double precision,
    geo_long double precision,
    location public.geometry,
    time_ns bigint,
    open_test_uuid uuid,
    mock_location boolean,
    CONSTRAINT enforce_dims_location CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((public.geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913))
);


ALTER TABLE public.geo_location OWNER TO rmbt;

--
-- Name: COLUMN geo_location.open_test_uuid; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.geo_location.open_test_uuid IS 'open uuid of the test';


--
-- Name: json_sender_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.json_sender_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.json_sender_uid_seq OWNER TO rmbt;

--
-- Name: json_sender; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.json_sender (
    uid integer DEFAULT nextval('public.json_sender_uid_seq'::regclass) NOT NULL,
    sender_id character varying(16),
    comment character varying(200),
    count bigint DEFAULT 0 NOT NULL
);


ALTER TABLE public.json_sender OWNER TO rmbt;

--
-- Name: kategorisierte_gemeinden; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.kategorisierte_gemeinden (
    gid integer NOT NULL,
    objectid integer,
    gemeinde_i integer,
    gemeinde character varying(40),
    bezirk_id smallint,
    bezirk character varying(40),
    land_id smallint,
    land character varying(25),
    staat character varying(2),
    lage_int smallint,
    lage character varying(12),
    ew_gesamt integer,
    shape_leng numeric,
    shape_area numeric,
    area integer,
    area_dsr integer,
    area_2100_ integer,
    area_21001 integer,
    area_dsr_2 integer,
    area_dsr_3 integer,
    pop_2100_i integer,
    pop_2100_1 integer,
    pop_dsr_21 integer,
    pop_dsr_22 integer,
    bs integer,
    bs_2100_in integer,
    bs_2100__1 integer,
    area_fn_4m integer,
    area_fn_8m integer,
    area_fn_ta integer,
    area_fn__1 integer,
    area_fn__2 integer,
    area_fn__3 integer,
    pop_fn_4_m integer,
    pop_fn_8mb integer,
    pop_fn_4mb integer,
    pop_fn_8_1 integer,
    pop_fn_4_1 integer,
    pop_fn_8_2 integer,
    kategorie integer,
    versorgung numeric,
    kat_neu character varying(50),
    fn_anteil_ numeric,
    fn_anteil1 numeric,
    anhang character varying(50),
    the_geom public.geometry,
    CONSTRAINT enforce_dims_the_geom CHECK ((public.st_ndims(the_geom) = 2)),
    CONSTRAINT enforce_geotype_the_geom CHECK (((public.geometrytype(the_geom) = 'MULTIPOLYGON'::text) OR (the_geom IS NULL))),
    CONSTRAINT enforce_srid_the_geom CHECK ((public.st_srid(the_geom) = 900913))
);


ALTER TABLE public.kategorisierte_gemeinden OWNER TO rmbt;

--
-- Name: kategorisierte_gemeinden_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.kategorisierte_gemeinden_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.kategorisierte_gemeinden_gid_seq OWNER TO rmbt;

--
-- Name: kategorisierte_gemeinden_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.kategorisierte_gemeinden_gid_seq OWNED BY public.kategorisierte_gemeinden.gid;


--
-- Name: location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.location_uid_seq OWNER TO rmbt;

--
-- Name: location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.location_uid_seq OWNED BY public.geo_location.uid;


--
-- Name: logged_actions; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.logged_actions (
    schema_name text NOT NULL,
    table_name text NOT NULL,
    user_name text,
    action_tstamp timestamp with time zone DEFAULT now() NOT NULL,
    action text NOT NULL,
    original_data text,
    new_data text,
    query text,
    CONSTRAINT logged_actions_action_check CHECK ((action = ANY (ARRAY['I'::text, 'D'::text, 'U'::text])))
)
WITH (fillfactor='100');


ALTER TABLE public.logged_actions OWNER TO rmbt;

--
-- Name: mcc2country; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.mcc2country (
    mcc character varying(3) NOT NULL,
    country character varying(2) NOT NULL
);


ALTER TABLE public.mcc2country OWNER TO rmbt;

--
-- Name: mccmnc2name; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.mccmnc2name (
    uid integer NOT NULL,
    mccmnc character varying(7) NOT NULL,
    valid_from date DEFAULT '0001-01-01'::date,
    valid_to date DEFAULT '9999-12-31'::date,
    country character varying(2),
    name character varying(200) NOT NULL,
    shortname character varying(100),
    use_for_sim boolean DEFAULT true,
    use_for_network boolean DEFAULT true,
    mcc_mnc_network_mapping character varying(10),
    comment character varying(200),
    mapped_uid integer
);


ALTER TABLE public.mccmnc2name OWNER TO rmbt;

--
-- Name: mccmnc2name_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.mccmnc2name_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mccmnc2name_uid_seq OWNER TO rmbt;

--
-- Name: mccmnc2name_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.mccmnc2name_uid_seq OWNED BY public.mccmnc2name.uid;


--
-- Name: mccmnc2provider; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.mccmnc2provider (
    uid integer NOT NULL,
    mcc_mnc_sim character varying(10),
    provider_id integer NOT NULL,
    mcc_mnc_network character varying(10),
    valid_from date,
    valid_to date
);


ALTER TABLE public.mccmnc2provider OWNER TO rmbt;

--
-- Name: mccmnc2provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.mccmnc2provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mccmnc2provider_uid_seq OWNER TO rmbt;

--
-- Name: mccmnc2provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.mccmnc2provider_uid_seq OWNED BY public.mccmnc2provider.uid;


--
-- Name: ne_10m_admin_0_countries; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.ne_10m_admin_0_countries (
    gid integer NOT NULL,
    scalerank smallint,
    featurecla character varying(30),
    labelrank double precision,
    sovereignt character varying(32),
    sov_a3 character varying(3),
    adm0_dif double precision,
    level double precision,
    type character varying(17),
    admin character varying(36),
    adm0_a3 character varying(3),
    geou_dif double precision,
    geounit character varying(36),
    gu_a3 character varying(3),
    su_dif double precision,
    subunit character varying(36),
    su_a3 character varying(3),
    brk_diff double precision,
    name character varying(36),
    name_long character varying(36),
    brk_a3 character varying(3),
    brk_name character varying(36),
    brk_group character varying(30),
    abbrev character varying(13),
    postal character varying(4),
    formal_en character varying(52),
    formal_fr character varying(35),
    name_ciawf character varying(45),
    note_adm0 character varying(22),
    note_brk character varying(164),
    name_sort character varying(36),
    name_alt character varying(38),
    mapcolor7 double precision,
    mapcolor8 double precision,
    mapcolor9 double precision,
    mapcolor13 double precision,
    pop_est double precision,
    pop_rank double precision,
    gdp_md_est double precision,
    pop_year double precision,
    lastcensus double precision,
    gdp_year double precision,
    economy character varying(26),
    income_grp character varying(23),
    wikipedia double precision,
    fips_10_ character varying(3),
    iso_a2 character varying(5),
    iso_a3 character varying(3),
    iso_a3_eh character varying(3),
    iso_n3 character varying(3),
    un_a3 character varying(4),
    wb_a2 character varying(3),
    wb_a3 character varying(3),
    woe_id double precision,
    woe_id_eh double precision,
    woe_note character varying(190),
    adm0_a3_is character varying(3),
    adm0_a3_us character varying(3),
    adm0_a3_un double precision,
    adm0_a3_wb double precision,
    continent character varying(23),
    region_un character varying(23),
    subregion character varying(25),
    region_wb character varying(26),
    name_len double precision,
    long_len double precision,
    abbrev_len double precision,
    tiny double precision,
    homepart double precision,
    min_zoom double precision,
    min_label double precision,
    max_label double precision,
    geom public.geometry(MultiPolygon,900913)
);


ALTER TABLE public.ne_10m_admin_0_countries OWNER TO rmbt;

--
-- Name: ne_10m_admin_0_countries_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.ne_10m_admin_0_countries_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ne_10m_admin_0_countries_gid_seq OWNER TO rmbt;

--
-- Name: ne_10m_admin_0_countries_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.ne_10m_admin_0_countries_gid_seq OWNED BY public.ne_10m_admin_0_countries.gid;


--
-- Name: network_type; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.network_type (
    uid integer NOT NULL,
    name character varying(200) NOT NULL,
    group_name character varying NOT NULL,
    aggregate character varying[],
    type character varying NOT NULL,
    technology_order integer DEFAULT 0 NOT NULL,
    min_speed_download_kbps integer,
    max_speed_download_kbps integer,
    min_speed_upload_kbps integer,
    max_speed_upload_kbps integer
);


ALTER TABLE public.network_type OWNER TO rmbt;

--
-- Name: network_type_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.network_type_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.network_type_uid_seq OWNER TO rmbt;

--
-- Name: network_type_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.network_type_uid_seq OWNED BY public.network_type.uid;


--
-- Name: news; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.news (
    uid integer NOT NULL,
    "time" timestamp with time zone NOT NULL,
    title_en text,
    title_de text,
    text_en text,
    text_de text,
    active boolean DEFAULT false NOT NULL,
    force boolean DEFAULT false NOT NULL,
    plattform text,
    max_software_version_code integer,
    min_software_version_code integer,
    uuid uuid
);


ALTER TABLE public.news OWNER TO rmbt;

--
-- Name: news_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.news_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.news_uid_seq OWNER TO rmbt;

--
-- Name: news_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.news_uid_seq OWNED BY public.news.uid;


--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 (
    gid integer NOT NULL,
    kg_nr character varying(6),
    kg character varying(50),
    meridian character varying(3),
    gkz character varying(6),
    pg character varying(50),
    bkz character varying(4),
    pb character varying(50),
    fa_nr character varying(3),
    fa character varying(50),
    gb_kz character varying(4),
    gb character varying(50),
    va_nr character varying(3),
    va character varying(50),
    bl_kz character varying(2),
    bl character varying(50),
    st_kz smallint,
    st character varying(50),
    fl double precision,
    geom public.geometry(MultiPolygon,31287),
    kg_nr_int integer
);


ALTER TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 OWNER TO rmbt;

--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq OWNER TO rmbt;

--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq OWNED BY public.oesterreich_bev_kg_lam_mitattribute_2017_10_02.gid;


--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02_old; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_old (
    gid integer,
    kg_nr character varying(6),
    kg character varying(50),
    meridian character varying(3),
    gkz character varying(6),
    pg character varying(50),
    bkz character varying(4),
    pb character varying(50),
    fa_nr character varying(3),
    fa character varying(50),
    gb_kz character varying(4),
    gb character varying(50),
    va_nr character varying(3),
    va character varying(50),
    bl_kz character varying(2),
    bl character varying(50),
    st_kz smallint,
    st character varying(50),
    fl double precision,
    geom public.geometry(MultiPolygon,31287),
    kg_nr_int integer
);


ALTER TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_old OWNER TO rmbt;

--
-- Name: oesterreich_bev_vgd_lam; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.oesterreich_bev_vgd_lam (
    gid integer NOT NULL,
    kg_nr character varying(6),
    kg character varying(50),
    meridian character varying(3),
    gkz character varying(6),
    pg character varying(50),
    bkz character varying(4),
    pb character varying(50),
    fa_nr character varying(3),
    fa character varying(50),
    gb_kz character varying(4),
    gb character varying(50),
    va_nr character varying(3),
    va character varying(50),
    bl_kz character varying(2),
    bl character varying(50),
    st_kz smallint,
    st character varying(50),
    fl double precision,
    geom public.geometry(MultiPolygon,31287)
);


ALTER TABLE public.oesterreich_bev_vgd_lam OWNER TO rmbt;

--
-- Name: oesterreich_bev_vgd_lam_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.oesterreich_bev_vgd_lam_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.oesterreich_bev_vgd_lam_gid_seq OWNER TO rmbt;

--
-- Name: oesterreich_bev_vgd_lam_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.oesterreich_bev_vgd_lam_gid_seq OWNED BY public.oesterreich_bev_vgd_lam.gid;


--
-- Name: ping; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.ping (
    uid bigint NOT NULL,
    test_id bigint,
    value bigint,
    value_server bigint,
    time_ns bigint,
    open_test_uuid uuid
);


ALTER TABLE public.ping OWNER TO rmbt;

--
-- Name: COLUMN ping.open_test_uuid; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.ping.open_test_uuid IS 'open uuid of the test';


--
-- Name: ping_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.ping_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ping_uid_seq OWNER TO rmbt;

--
-- Name: ping_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.ping_uid_seq OWNED BY public.ping.uid;


--
-- Name: provider; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.provider (
    uid integer NOT NULL,
    name character varying(200),
    mcc_mnc character varying(10),
    shortname character varying(100),
    map_filter boolean NOT NULL
);


ALTER TABLE public.provider OWNER TO rmbt;

--
-- Name: provider_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.provider_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.provider_uid_seq OWNER TO rmbt;

--
-- Name: provider_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.provider_uid_seq OWNED BY public.provider.uid;


--
-- Name: qos_test_desc; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qos_test_desc (
    uid integer NOT NULL,
    desc_key text,
    value text,
    lang text
);


ALTER TABLE public.qos_test_desc OWNER TO rmbt;

--
-- Name: qos_test_desc_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qos_test_desc_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qos_test_desc_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_desc_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qos_test_desc_uid_seq OWNED BY public.qos_test_desc.uid;


--
-- Name: qos_test_objective; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qos_test_objective (
    uid integer NOT NULL,
    test public.qostest NOT NULL,
    test_class integer,
    test_server integer,
    concurrency_group integer DEFAULT 0 NOT NULL,
    test_desc text,
    test_summary text,
    param json DEFAULT '{}'::json NOT NULL,
    results json
);


ALTER TABLE public.qos_test_objective OWNER TO rmbt;

--
-- Name: qos_test_objective_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qos_test_objective_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qos_test_objective_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_objective_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qos_test_objective_uid_seq OWNED BY public.qos_test_objective.uid;


--
-- Name: qos_test_result; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qos_test_result (
    uid integer NOT NULL,
    test_uid bigint,
    qos_test_uid bigint,
    success_count integer DEFAULT 0 NOT NULL,
    failure_count integer DEFAULT 0 NOT NULL,
    implausible boolean DEFAULT false,
    deleted boolean DEFAULT false,
    result json
);


ALTER TABLE public.qos_test_result OWNER TO rmbt;

--
-- Name: qos_test_result_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qos_test_result_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qos_test_result_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_result_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qos_test_result_uid_seq OWNED BY public.qos_test_result.uid;


--
-- Name: qos_test_type_desc; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qos_test_type_desc (
    uid integer NOT NULL,
    test public.qostest,
    test_desc text,
    test_name text
);


ALTER TABLE public.qos_test_type_desc OWNER TO rmbt;

--
-- Name: qos_test_type_desc_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qos_test_type_desc_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qos_test_type_desc_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_type_desc_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qos_test_type_desc_uid_seq OWNED BY public.qos_test_type_desc.uid;


--
-- Name: radio_cell; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.radio_cell (
    uid integer NOT NULL,
    uuid uuid NOT NULL,
    open_test_uuid uuid,
    technology character varying(10),
    mnc integer,
    mcc integer,
    location_id integer,
    area_code integer,
    primary_scrambling_code integer,
    registered boolean,
    channel_number integer,
    active boolean
);


ALTER TABLE public.radio_cell OWNER TO rmbt;

--
-- Name: radio_cell_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.radio_cell_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.radio_cell_uid_seq OWNER TO rmbt;

--
-- Name: radio_cell_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.radio_cell_uid_seq OWNED BY public.radio_cell.uid;


--
-- Name: radio_signal; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.radio_signal (
    uid integer NOT NULL,
    cell_uuid uuid NOT NULL,
    open_test_uuid uuid,
    time_ns bigint,
    time_ns_last bigint,
    "time" timestamp with time zone,
    signal_strength integer,
    lte_rsrp integer,
    lte_rsrq integer,
    lte_rssnr integer,
    lte_cqi integer,
    bit_error_rate integer,
    timing_advance integer,
    wifi_link_speed integer,
    network_type_id integer
);


ALTER TABLE public.radio_signal OWNER TO rmbt;

--
-- Name: radio_signal_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.radio_signal_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.radio_signal_uid_seq OWNER TO rmbt;

--
-- Name: radio_signal_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.radio_signal_uid_seq OWNED BY public.radio_signal.uid;


--
-- Name: settings; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.settings (
    uid integer NOT NULL,
    key character varying NOT NULL,
    lang character(2),
    value character varying NOT NULL
);


ALTER TABLE public.settings OWNER TO rmbt;

--
-- Name: settings_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.settings_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.settings_uid_seq OWNER TO rmbt;

--
-- Name: settings_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.settings_uid_seq OWNED BY public.settings.uid;


--
-- Name: signal; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.signal (
    uid bigint NOT NULL,
    test_id bigint,
    "time" timestamp with time zone,
    signal_strength integer,
    network_type_id integer,
    wifi_link_speed integer,
    gsm_bit_error_rate integer,
    wifi_rssi integer,
    time_ns bigint,
    lte_rsrp integer,
    lte_rsrq integer,
    lte_rssnr integer,
    lte_cqi integer,
    open_test_uuid uuid
);


ALTER TABLE public.signal OWNER TO rmbt;

--
-- Name: COLUMN signal.open_test_uuid; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.signal.open_test_uuid IS 'open uuid of the test';


--
-- Name: signal_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.signal_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.signal_uid_seq OWNER TO rmbt;

--
-- Name: signal_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.signal_uid_seq OWNED BY public.signal.uid;


--
-- Name: speed; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.speed (
    open_test_uuid uuid NOT NULL,
    items jsonb
);


ALTER TABLE public.speed OWNER TO rmbt;

--
-- Name: TABLE speed; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON TABLE public.speed IS 'speed items of all tests';


--
-- Name: COLUMN speed.open_test_uuid; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.speed.open_test_uuid IS 'uuid of the test';


--
-- Name: COLUMN speed.items; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.speed.items IS 'speed items of the test';


--
-- Name: statistik_austria_gem_20180101; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.statistik_austria_gem_20180101 (
    gid integer NOT NULL,
    id character varying(254),
    name character varying(254),
    geom public.geometry(MultiPolygon,31287)
);


ALTER TABLE public.statistik_austria_gem_20180101 OWNER TO rmbt;

--
-- Name: statistik_austria_gem_20180101_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.statistik_austria_gem_20180101_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.statistik_austria_gem_20180101_gid_seq OWNER TO rmbt;

--
-- Name: statistik_austria_gem_20180101_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.statistik_austria_gem_20180101_gid_seq OWNED BY public.statistik_austria_gem_20180101.gid;


--
-- Name: status; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.status (
    uid integer NOT NULL,
    client_uuid uuid NOT NULL,
    "time" timestamp with time zone,
    plattform character varying(50),
    model character varying(50),
    product character varying(50),
    device character varying(50),
    software_version_code character varying(50),
    api_level character varying(10),
    ip character varying(50),
    age bigint,
    lat double precision,
    long double precision,
    accuracy double precision,
    altitude double precision,
    speed double precision,
    provider character varying(50),
    signalnetworktypeid double precision,
    signalwifirssi double precision,
    signalltersrp double precision,
    signalltersrq double precision,
    signalrssi double precision,
    signalltecqi double precision,
    signaltime bigint
);


ALTER TABLE public.status OWNER TO rmbt;

--
-- Name: status_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.status_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.status_uid_seq OWNER TO rmbt;

--
-- Name: status_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.status_uid_seq OWNED BY public.status.uid;


--
-- Name: sync_group; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.sync_group (
    uid integer NOT NULL,
    tstamp timestamp with time zone NOT NULL
);


ALTER TABLE public.sync_group OWNER TO rmbt;

--
-- Name: sync_group_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.sync_group_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.sync_group_uid_seq OWNER TO rmbt;

--
-- Name: sync_group_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.sync_group_uid_seq OWNED BY public.sync_group.uid;


--
-- Name: test; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test (
    uid bigint NOT NULL,
    uuid uuid,
    client_id bigint,
    client_version character varying(10),
    client_name character varying,
    client_language character varying(10),
    token character varying(500),
    server_id integer,
    port integer,
    use_ssl boolean DEFAULT false NOT NULL,
    "time" timestamp with time zone,
    speed_upload integer,
    speed_download integer,
    ping_shortest bigint,
    encryption character varying(50),
    client_public_ip character varying(100),
    plattform character varying(200),
    os_version character varying(100),
    api_level character varying(10),
    device character varying(200),
    model character varying(200),
    product character varying(200),
    phone_type integer,
    data_state integer,
    network_country character varying(10),
    network_operator character varying(10),
    network_operator_name character varying(200),
    network_sim_country character varying(10),
    network_sim_operator character varying(10),
    network_sim_operator_name character varying(200),
    wifi_ssid character varying(200),
    wifi_bssid character varying(200),
    wifi_network_id character varying(200),
    duration integer,
    num_threads integer,
    status character varying(100),
    timezone character varying(200),
    bytes_download bigint,
    bytes_upload bigint,
    nsec_download bigint,
    nsec_upload bigint,
    server_ip character varying(100),
    client_software_version character varying(100),
    geo_lat double precision,
    geo_long double precision,
    network_type integer,
    location public.geometry,
    signal_strength integer,
    software_revision character varying(200),
    client_test_counter bigint,
    nat_type character varying(200),
    client_previous_test_status character varying(200),
    public_ip_asn bigint,
    speed_upload_log double precision,
    speed_download_log double precision,
    total_bytes_download bigint,
    total_bytes_upload bigint,
    wifi_link_speed integer,
    public_ip_rdns character varying(200),
    public_ip_as_name character varying(200),
    test_slot integer,
    provider_id integer,
    network_is_roaming boolean,
    ping_shortest_log double precision,
    run_ndt boolean,
    num_threads_requested integer,
    client_public_ip_anonymized character varying(100),
    zip_code integer,
    geo_provider character varying(200),
    geo_accuracy double precision,
    deleted boolean DEFAULT false NOT NULL,
    comment text,
    open_uuid uuid,
    client_time timestamp with time zone,
    zip_code_geo integer,
    mobile_provider_id integer,
    roaming_type integer,
    open_test_uuid uuid,
    country_asn character(2),
    country_location character(2),
    test_if_bytes_download bigint,
    test_if_bytes_upload bigint,
    implausible boolean DEFAULT false NOT NULL,
    testdl_if_bytes_download bigint,
    testdl_if_bytes_upload bigint,
    testul_if_bytes_download bigint,
    testul_if_bytes_upload bigint,
    country_geoip character(2),
    location_max_distance integer,
    location_max_distance_gps integer,
    network_group_name character varying(200),
    network_group_type character varying(200),
    time_dl_ns bigint,
    time_ul_ns bigint,
    num_threads_ul integer,
    "timestamp" timestamp without time zone DEFAULT now(),
    source_ip character varying(50),
    lte_rsrp integer,
    lte_rsrq integer,
    mobile_network_id integer,
    mobile_sim_id integer,
    dist_prev double precision,
    speed_prev double precision,
    tag character varying(512),
    ping_median bigint,
    ping_median_log double precision,
    source_ip_anonymized character varying(50),
    client_ip_local character varying(50),
    client_ip_local_anonymized character varying(50),
    client_ip_local_type character varying(50),
    hidden_code character varying(8),
    origin uuid,
    developer_code character varying(8),
    dual_sim boolean,
    gkz integer,
    android_permissions json,
    dual_sim_detection_method character varying(50),
    pinned boolean DEFAULT true NOT NULL,
    similar_test_uid bigint,
    user_server_selection boolean,
    radio_band smallint,
    sim_count smallint,
    time_qos_ns bigint,
    test_nsec_qos bigint,
    channel_number integer,
    gkz_bev integer,
    gkz_sa integer,
    kg_nr_bev integer,
    land_cover integer,
    cell_location_id integer,
    cell_area_code integer,
    settlement_type integer,
    CONSTRAINT enforce_dims_location CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((public.geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913)),
    CONSTRAINT test_speed_download_noneg CHECK ((speed_download >= 0)),
	CONSTRAINT settlement_type_check CHECK ( settlement_type > 0 and settlement_type < 4),
    CONSTRAINT test_speed_upload_noneg CHECK ((speed_upload >= 0))
);


ALTER TABLE public.test OWNER TO rmbt;

--
-- Name: COLUMN test.server_id; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test.server_id IS 'id of test server used';


--
-- Name: test_loopmode; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_loopmode (
    uid integer NOT NULL,
    test_uuid uuid,
    client_uuid uuid,
    loop_uuid uuid,
    max_movement integer,
    max_delay integer,
    max_tests integer,
    test_counter integer
);


ALTER TABLE public.test_loopmode OWNER TO rmbt;

--
-- Name: test_loopmode_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_loopmode_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_loopmode_uid_seq OWNER TO rmbt;

--
-- Name: test_loopmode_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_loopmode_uid_seq OWNED BY public.test_loopmode.uid;


--
-- Name: test_ndt; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_ndt (
    uid integer NOT NULL,
    test_id bigint,
    s2cspd double precision,
    c2sspd double precision,
    avgrtt double precision,
    main text,
    stat text,
    diag text,
    time_ns bigint,
    time_end_ns bigint
);


ALTER TABLE public.test_ndt OWNER TO rmbt;

--
-- Name: test_ndt_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_ndt_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_ndt_uid_seq OWNER TO rmbt;

--
-- Name: test_ndt_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_ndt_uid_seq OWNED BY public.test_ndt.uid;


--
-- Name: test_server; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_server (
    uid integer NOT NULL,
    name character varying(200),
    web_address character varying(500),
    port integer,
    port_ssl integer,
    city character varying,
    country character varying,
    geo_lat double precision,
    geo_long double precision,
    location public.geometry(Point,900913),
    web_address_ipv4 character varying(200),
    web_address_ipv6 character varying(200),
    server_type character varying(10),
    priority integer DEFAULT 0 NOT NULL,
    weight integer DEFAULT 1 NOT NULL,
    active boolean DEFAULT true NOT NULL,
    uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    key character varying,
    selectable boolean DEFAULT false NOT NULL,
    countries character varying[] DEFAULT '{dev}'::character varying[] NOT NULL,
    node character varying,
    CONSTRAINT enforce_dims_location CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((public.geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913))
);


ALTER TABLE public.test_server OWNER TO rmbt;

--
-- Name: test_server_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_server_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_server_uid_seq OWNER TO rmbt;

--
-- Name: test_server_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_server_uid_seq OWNED BY public.test_server.uid;


--
-- Name: test_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_uid_seq OWNER TO rmbt;

--
-- Name: test_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_uid_seq OWNED BY public.test.uid;


--
-- Name: v_dl_bandwidth_per_minute; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW public.v_dl_bandwidth_per_minute AS
 SELECT date_trunc('minute'::text, test."time") AS time_minute,
    count(*) AS test_count,
    (sum(test.speed_download) / 1000) AS bandwidth_dl_mbps
   FROM public.test
  WHERE ((test.status)::text = 'FINISHED'::text)
  GROUP BY (date_trunc('minute'::text, test."time"))
  ORDER BY (date_trunc('minute'::text, test."time")) DESC;


ALTER TABLE public.v_dl_bandwidth_per_minute OWNER TO rmbt;

--
-- Name: v_get_replication_delay; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_get_replication_delay AS
 SELECT get_replication_delay.pid,
    get_replication_delay.usesysid,
    get_replication_delay.usename,
    get_replication_delay.application_name,
    get_replication_delay.client_addr,
    get_replication_delay.client_hostname,
    get_replication_delay.client_port,
    get_replication_delay.backend_start,
    get_replication_delay.backend_xmin,
    get_replication_delay.state,
    get_replication_delay.sent_lsn,
    get_replication_delay.write_lsn,
    get_replication_delay.flush_lsn,
    get_replication_delay.replay_lsn,
    get_replication_delay.write_lag,
    get_replication_delay.flush_lag,
    get_replication_delay.replay_lag,
    get_replication_delay.sync_priority,
    get_replication_delay.sync_state
   FROM public.get_replication_delay() get_replication_delay(pid, usesysid, usename, application_name, client_addr, client_hostname, client_port, backend_start, backend_xmin, state, sent_lsn, write_lsn, flush_lsn, replay_lsn, write_lag, flush_lag, replay_lag, sync_priority, sync_state);


ALTER TABLE public.v_get_replication_delay OWNER TO postgres;

--
-- Name: v_test; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_test AS
 SELECT test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.token,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.speed_upload_log,
    test.speed_download_log,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id,
    test.dist_prev,
    test.speed_prev,
    test.tag,
    test.ping_median,
    test.ping_median_log,
    test.source_ip_anonymized,
    test.client_ip_local,
    test.client_ip_local_anonymized,
    test.client_ip_local_type,
    COALESCE((test.lte_rsrp + 10), test.signal_strength) AS merged_signal,
    test.developer_code,
    test.gkz
   FROM public.test;


ALTER TABLE public.v_test OWNER TO postgres;

--
-- Name: v_test2; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_test2 AS
 SELECT test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.token,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.speed_upload_log,
    test.speed_download_log,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id,
    test.dist_prev,
    test.speed_prev,
    test.tag,
    test.ping_median,
    test.ping_median_log,
    test.source_ip_anonymized,
    test.client_ip_local,
    test.client_ip_local_anonymized,
    test.client_ip_local_type,
    COALESCE((test.lte_rsrp + 10), test.signal_strength) AS merged_signal,
    test.gkz,
    test.user_server_selection
   FROM public.test;


ALTER TABLE public.v_test2 OWNER TO postgres;

--
-- Name: v_test3; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_test3 AS
 SELECT test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.token,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.speed_upload_log,
    test.speed_download_log,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id,
    test.dist_prev,
    test.speed_prev,
    test.tag,
    test.ping_median,
    test.ping_median_log,
    test.source_ip_anonymized,
    test.client_ip_local,
    test.client_ip_local_anonymized,
    test.client_ip_local_type,
    COALESCE((test.lte_rsrp + 10), test.signal_strength) AS merged_signal,
    test.gkz,
    test.kg_nr_bev,
    test.user_server_selection
   FROM public.test;


ALTER TABLE public.v_test3 OWNER TO postgres;

--
-- Name: vt; Type: VIEW; Schema: public; Owner: dj
--

CREATE VIEW public.vt AS
 SELECT test.uid,
    test.uuid,
    test.client_id,
    test.client_version,
    test.client_name,
    test.client_language,
    test.server_id,
    test.port,
    test.use_ssl,
    test."time",
    test.speed_upload,
    test.speed_download,
    test.ping_shortest,
    test.encryption,
    test.client_public_ip,
    test.plattform,
    test.os_version,
    test.api_level,
    test.device,
    test.model,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.wifi_ssid,
    test.wifi_bssid,
    test.wifi_network_id,
    test.duration,
    test.num_threads,
    test.status,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.client_software_version,
    test.geo_lat,
    test.geo_long,
    test.network_type,
    test.location,
    test.signal_strength,
    test.software_revision,
    test.client_test_counter,
    test.nat_type,
    test.client_previous_test_status,
    test.public_ip_asn,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_rdns,
    test.public_ip_as_name,
    test.test_slot,
    test.provider_id,
    test.network_is_roaming,
    test.ping_shortest_log,
    test.run_ndt,
    test.num_threads_requested,
    test.client_public_ip_anonymized,
    test.zip_code,
    test.geo_provider,
    test.geo_accuracy,
    test.deleted,
    test.comment,
    test.open_uuid,
    test.client_time,
    test.zip_code_geo,
    test.mobile_provider_id,
    test.roaming_type,
    test.open_test_uuid,
    test.country_asn,
    test.country_location,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.implausible,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.location_max_distance,
    test.location_max_distance_gps,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    test."timestamp",
    test.source_ip,
    test.lte_rsrp,
    test.lte_rsrq,
    test.mobile_network_id,
    test.mobile_sim_id,
    test.dist_prev,
    test.speed_prev,
    test.tag,
    test.ping_median,
    test.source_ip_anonymized,
    test.client_ip_local,
    test.client_ip_local_anonymized,
    test.client_ip_local_type,
    test.hidden_code,
    test.origin,
    test.developer_code,
    test.dual_sim,
    test.gkz,
    test.android_permissions,
    test.dual_sim_detection_method,
    test.pinned,
    test.similar_test_uid
   FROM public.test;


ALTER TABLE public.vt OWNER TO dj;

--
-- Name: VIEW vt; Type: COMMENT; Schema: public; Owner: dj
--

COMMENT ON VIEW public.vt IS 'light weight columns from test (dj)';


--
-- Name: as2provider uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.as2provider ALTER COLUMN uid SET DEFAULT nextval('public.as2provider_uid_seq'::regclass);


--
-- Name: cell_location uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cell_location ALTER COLUMN uid SET DEFAULT nextval('public.cell_location_uid_seq'::regclass);


--
-- Name: clc12_all_oesterreich gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.clc12_all_oesterreich ALTER COLUMN gid SET DEFAULT nextval('public.clc12_all_oesterreich_gid_seq'::regclass);


--
-- Name: client uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client ALTER COLUMN uid SET DEFAULT nextval('public.client_uid_seq'::regclass);


--
-- Name: client_type uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client_type ALTER COLUMN uid SET DEFAULT nextval('public.client_type_uid_seq'::regclass);


--
-- Name: device_map uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map ALTER COLUMN uid SET DEFAULT nextval('public.android_device_map_uid_seq'::regclass);


--
-- Name: geo_location uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location ALTER COLUMN uid SET DEFAULT nextval('public.location_uid_seq'::regclass);


--
-- Name: kategorisierte_gemeinden gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.kategorisierte_gemeinden ALTER COLUMN gid SET DEFAULT nextval('public.kategorisierte_gemeinden_gid_seq'::regclass);


--
-- Name: mccmnc2name uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mccmnc2name ALTER COLUMN uid SET DEFAULT nextval('public.mccmnc2name_uid_seq'::regclass);


--
-- Name: mccmnc2provider uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mccmnc2provider ALTER COLUMN uid SET DEFAULT nextval('public.mccmnc2provider_uid_seq'::regclass);


--
-- Name: ne_10m_admin_0_countries gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ne_10m_admin_0_countries ALTER COLUMN gid SET DEFAULT nextval('public.ne_10m_admin_0_countries_gid_seq'::regclass);


--
-- Name: network_type uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.network_type ALTER COLUMN uid SET DEFAULT nextval('public.network_type_uid_seq'::regclass);


--
-- Name: news uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.news ALTER COLUMN uid SET DEFAULT nextval('public.news_uid_seq'::regclass);


--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02 gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 ALTER COLUMN gid SET DEFAULT nextval('public.oesterreich_bev_kg_lam_mitattribute_2017_10_02_gid_seq'::regclass);


--
-- Name: oesterreich_bev_vgd_lam gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.oesterreich_bev_vgd_lam ALTER COLUMN gid SET DEFAULT nextval('public.oesterreich_bev_vgd_lam_gid_seq'::regclass);


--
-- Name: ping uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ping ALTER COLUMN uid SET DEFAULT nextval('public.ping_uid_seq'::regclass);


--
-- Name: provider uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.provider ALTER COLUMN uid SET DEFAULT nextval('public.provider_uid_seq'::regclass);


--
-- Name: qos_test_desc uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_desc ALTER COLUMN uid SET DEFAULT nextval('public.qos_test_desc_uid_seq'::regclass);


--
-- Name: qos_test_objective uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_objective ALTER COLUMN uid SET DEFAULT nextval('public.qos_test_objective_uid_seq'::regclass);


--
-- Name: qos_test_result uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result ALTER COLUMN uid SET DEFAULT nextval('public.qos_test_result_uid_seq'::regclass);


--
-- Name: qos_test_type_desc uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_type_desc ALTER COLUMN uid SET DEFAULT nextval('public.qos_test_type_desc_uid_seq'::regclass);


--
-- Name: radio_cell uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_cell ALTER COLUMN uid SET DEFAULT nextval('public.radio_cell_uid_seq'::regclass);


--
-- Name: radio_signal uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal ALTER COLUMN uid SET DEFAULT nextval('public.radio_signal_uid_seq'::regclass);


--
-- Name: settings uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.settings ALTER COLUMN uid SET DEFAULT nextval('public.settings_uid_seq'::regclass);


--
-- Name: signal uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal ALTER COLUMN uid SET DEFAULT nextval('public.signal_uid_seq'::regclass);


--
-- Name: statistik_austria_gem_20180101 gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.statistik_austria_gem_20180101 ALTER COLUMN gid SET DEFAULT nextval('public.statistik_austria_gem_20180101_gid_seq'::regclass);


--
-- Name: status uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.status ALTER COLUMN uid SET DEFAULT nextval('public.status_uid_seq'::regclass);


--
-- Name: sync_group uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.sync_group ALTER COLUMN uid SET DEFAULT nextval('public.sync_group_uid_seq'::regclass);


--
-- Name: test uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test ALTER COLUMN uid SET DEFAULT nextval('public.test_uid_seq'::regclass);


--
-- Name: test_loopmode uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode ALTER COLUMN uid SET DEFAULT nextval('public.test_loopmode_uid_seq'::regclass);


--
-- Name: test_ndt uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_ndt ALTER COLUMN uid SET DEFAULT nextval('public.test_ndt_uid_seq'::regclass);


--
-- Name: test_server uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_server ALTER COLUMN uid SET DEFAULT nextval('public.test_server_uid_seq'::regclass);


--
-- Name: device_map android_device_map_codename_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map
    ADD CONSTRAINT android_device_map_codename_key UNIQUE (codename);


--
-- Name: device_map android_device_map_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map
    ADD CONSTRAINT android_device_map_pkey PRIMARY KEY (uid);


--
-- Name: as2provider as2provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.as2provider
    ADD CONSTRAINT as2provider_pkey PRIMARY KEY (uid);


--
-- Name: cell_location cell_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cell_location
    ADD CONSTRAINT cell_location_pkey PRIMARY KEY (uid);


--
-- Name: clc12_all_oesterreich clc12_all_oesterreich_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.clc12_all_oesterreich
    ADD CONSTRAINT clc12_all_oesterreich_pkey PRIMARY KEY (gid);


--
-- Name: client client_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_pkey PRIMARY KEY (uid);


--
-- Name: client client_sync_code; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_sync_code UNIQUE (sync_code);


--
-- Name: client_type client_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client_type
    ADD CONSTRAINT client_type_pkey PRIMARY KEY (uid);


--
-- Name: client client_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_uuid_key UNIQUE (uuid);


--
-- Name: device_map device_map_fullname_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map
    ADD CONSTRAINT device_map_fullname_key UNIQUE (fullname);


--
-- Name: json_sender json_sender_sender_id_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.json_sender
    ADD CONSTRAINT json_sender_sender_id_key UNIQUE (sender_id);


--
-- Name: kategorisierte_gemeinden kategorisierte_gemeinden_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.kategorisierte_gemeinden
    ADD CONSTRAINT kategorisierte_gemeinden_pkey PRIMARY KEY (gid);


--
-- Name: geo_location location_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location
    ADD CONSTRAINT location_pkey PRIMARY KEY (uid);


--
-- Name: mcc2country mcc2country_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mcc2country
    ADD CONSTRAINT mcc2country_pkey PRIMARY KEY (mcc);


--
-- Name: mccmnc2name mccmnc2name_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mccmnc2name
    ADD CONSTRAINT mccmnc2name_pkey PRIMARY KEY (uid);


--
-- Name: mccmnc2provider mccmnc2provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mccmnc2provider
    ADD CONSTRAINT mccmnc2provider_pkey PRIMARY KEY (uid);


--
-- Name: ne_10m_admin_0_countries ne_10m_admin_0_countries_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ne_10m_admin_0_countries
    ADD CONSTRAINT ne_10m_admin_0_countries_pkey PRIMARY KEY (gid);


--
-- Name: network_type network_type_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.network_type
    ADD CONSTRAINT network_type_pkey PRIMARY KEY (uid);


--
-- Name: oesterreich_bev_kg_lam_mitattribute_2017_10_02 oesterreich_bev_kg_lam_mitattribute_2017_10_02_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.oesterreich_bev_kg_lam_mitattribute_2017_10_02
    ADD CONSTRAINT oesterreich_bev_kg_lam_mitattribute_2017_10_02_pkey PRIMARY KEY (gid);


--
-- Name: oesterreich_bev_vgd_lam oesterreich_bev_vgd_lam_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.oesterreich_bev_vgd_lam
    ADD CONSTRAINT oesterreich_bev_vgd_lam_pkey PRIMARY KEY (gid);


--
-- Name: ping ping_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ping
    ADD CONSTRAINT ping_pkey PRIMARY KEY (uid);


--
-- Name: provider provider_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.provider
    ADD CONSTRAINT provider_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_desc qos_test_desc_desc_key_lang_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_desc
    ADD CONSTRAINT qos_test_desc_desc_key_lang_key UNIQUE (desc_key, lang);


--
-- Name: qos_test_desc qos_test_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_desc
    ADD CONSTRAINT qos_test_desc_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_objective qos_test_objective_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_objective
    ADD CONSTRAINT qos_test_objective_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_result qos_test_result_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result
    ADD CONSTRAINT qos_test_result_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_type_desc qos_test_type_desc_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_type_desc
    ADD CONSTRAINT qos_test_type_desc_pkey PRIMARY KEY (uid);


--
-- Name: qos_test_type_desc qos_test_type_desc_test_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_type_desc
    ADD CONSTRAINT qos_test_type_desc_test_key UNIQUE (test);


--
-- Name: radio_cell radio_cell_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_cell
    ADD CONSTRAINT radio_cell_pkey PRIMARY KEY (uid);


--
-- Name: radio_signal radio_signal_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal
    ADD CONSTRAINT radio_signal_pkey PRIMARY KEY (uid);


--
-- Name: settings settings_key_lang_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_key_lang_key UNIQUE (key, lang);


--
-- Name: settings settings_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.settings
    ADD CONSTRAINT settings_pkey PRIMARY KEY (uid);


--
-- Name: signal signal_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal
    ADD CONSTRAINT signal_pkey PRIMARY KEY (uid);


--
-- Name: speed speed_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.speed
    ADD CONSTRAINT speed_pkey PRIMARY KEY (open_test_uuid);


--
-- Name: statistik_austria_gem_20180101 statistik_austria_gem_20180101_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.statistik_austria_gem_20180101
    ADD CONSTRAINT statistik_austria_gem_20180101_pkey PRIMARY KEY (gid);


--
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (uid);


--
-- Name: sync_group sync_group_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.sync_group
    ADD CONSTRAINT sync_group_pkey PRIMARY KEY (uid);


--
-- Name: test_loopmode test_loopmode_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode
    ADD CONSTRAINT test_loopmode_pkey PRIMARY KEY (uid);


--
-- Name: test_ndt test_ndt_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_ndt
    ADD CONSTRAINT test_ndt_pkey PRIMARY KEY (uid);


--
-- Name: test_ndt test_ndt_test_id_unique; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_ndt
    ADD CONSTRAINT test_ndt_test_id_unique UNIQUE (test_id);


--
-- Name: test test_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_pkey PRIMARY KEY (uid);


--
-- Name: test_server test_server_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_server
    ADD CONSTRAINT test_server_pkey PRIMARY KEY (uid);


--
-- Name: test_server test_server_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_server
    ADD CONSTRAINT test_server_uuid_key UNIQUE (uuid);


--
-- Name: test test_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_uuid_key UNIQUE (uuid);


--
-- Name: news uid; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.news
    ADD CONSTRAINT uid PRIMARY KEY (uid);


--
-- Name: as2provider_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX as2provider_provider_id_idx ON public.as2provider USING btree (provider_id);


--
-- Name: cell_location_test_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cell_location_test_id_idx ON public.cell_location USING btree (test_id);


--
-- Name: cell_location_test_id_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cell_location_test_id_time_idx ON public.cell_location USING btree (test_id, "time");


--
-- Name: clc12_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX clc12_gix ON public.clc12_all_oesterreich USING gist (geom);


--
-- Name: clc_legend_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX clc_legend_idx ON public.clc_legend USING btree (clc_code);


--
-- Name: client_client_type_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX client_client_type_id_idx ON public.client USING btree (client_type_id);


--
-- Name: client_sync_group_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX client_sync_group_id_idx ON public.client USING btree (sync_group_id);


--
-- Name: download_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX download_idx ON public.test USING btree (bytes_download, network_type);


--
-- Name: fki_qos_test_result_qos_test_uid_fkey; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_qos_test_uid_fkey ON public.qos_test_result USING btree (qos_test_uid);


--
-- Name: fki_qos_test_result_test_uid; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_test_uid ON public.qos_test_result USING btree (test_uid);


--
-- Name: geo_location_location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_location_idx ON public.geo_location USING gist (location);


--
-- Name: geo_location_test_id_key; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_test_id_key ON public.geo_location USING btree (test_id);


--
-- Name: geo_location_test_id_provider; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_test_id_provider ON public.geo_location USING btree (test_id, provider);


--
-- Name: geo_location_test_id_provider_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_test_id_provider_time_idx ON public.geo_location USING btree (test_id, provider, "time");


--
-- Name: geo_location_test_id_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_test_id_time_idx ON public.geo_location USING btree (test_id, "time");


--
-- Name: gkz_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX gkz_bev_idx ON public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 USING btree (gkz);


--
-- Name: kategorisierte_gemeinden_gemeinde_i; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX kategorisierte_gemeinden_gemeinde_i ON public.kategorisierte_gemeinden USING btree (gemeinde_i);


--
-- Name: kategorisierte_gemeinden_the_geom_gist; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX kategorisierte_gemeinden_the_geom_gist ON public.kategorisierte_gemeinden USING gist (the_geom);


--
-- Name: kg_nr_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX kg_nr_bev_idx ON public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 USING btree (kg_nr);


--
-- Name: kg_nr_int_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX kg_nr_int_bev_idx ON public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 USING btree (kg_nr_int);


--
-- Name: location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX location_idx ON public.test USING gist (location);


--
-- Name: logged_actions_action_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX logged_actions_action_idx ON public.logged_actions USING btree (action);


--
-- Name: logged_actions_action_tstamp_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX logged_actions_action_tstamp_idx ON public.logged_actions USING btree (action_tstamp);


--
-- Name: logged_actions_schema_table_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX logged_actions_schema_table_idx ON public.logged_actions USING btree ((((schema_name || '.'::text) || table_name)));


--
-- Name: mcc2country_mcc; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX mcc2country_mcc ON public.mcc2country USING btree (mcc);


--
-- Name: mccmnc2name_mccmnc; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX mccmnc2name_mccmnc ON public.mccmnc2name USING btree (mccmnc);


--
-- Name: mccmnc2provider_mcc_mnc_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX mccmnc2provider_mcc_mnc_idx ON public.mccmnc2provider USING btree (mcc_mnc_sim, mcc_mnc_network);


--
-- Name: mccmnc2provider_provider_id; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX mccmnc2provider_provider_id ON public.mccmnc2provider USING btree (provider_id);


--
-- Name: ne_10m_admin_0_countries_iso_a2_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX ne_10m_admin_0_countries_iso_a2_idx ON public.ne_10m_admin_0_countries USING btree (iso_a2);


--
-- Name: ne_10m_admin_0_countries_iso_geom_gist; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX ne_10m_admin_0_countries_iso_geom_gist ON public.ne_10m_admin_0_countries USING gist (geom);


--
-- Name: network_type_group_name_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX network_type_group_name_idx ON public.network_type USING btree (group_name);


--
-- Name: network_type_type_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX network_type_type_idx ON public.network_type USING btree (type);


--
-- Name: news_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX news_time_idx ON public.news USING btree ("time");


--
-- Name: oesterreich_bev_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX oesterreich_bev_gix ON public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 USING gist (geom);


--
-- Name: open_test_uuid_cell_location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_cell_location_idx ON public.cell_location USING btree (open_test_uuid);


--
-- Name: open_test_uuid_geo_location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_geo_location_idx ON public.geo_location USING btree (open_test_uuid);


--
-- Name: open_test_uuid_ping_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_ping_idx ON public.ping USING btree (open_test_uuid);


--
-- Name: open_test_uuid_signal_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_signal_idx ON public.signal USING btree (open_test_uuid);


--
-- Name: ping_test_id_key; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX ping_test_id_key ON public.ping USING btree (test_id);


--
-- Name: provider_mcc_mnc_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX provider_mcc_mnc_idx ON public.provider USING btree (mcc_mnc);


--
-- Name: qos_test_desc_desc_key_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX qos_test_desc_desc_key_idx ON public.qos_test_desc USING btree (desc_key);


--
-- Name: radio_cell_open_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_cell_open_uuid_idx ON public.radio_cell USING btree (open_test_uuid);


--
-- Name: radio_cell_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE UNIQUE INDEX radio_cell_uuid_idx ON public.radio_cell USING btree (uuid);


--
-- Name: radio_signal_open_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_signal_open_uuid_idx ON public.radio_signal USING btree (open_test_uuid);


--
-- Name: settings_key_lang_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX settings_key_lang_idx ON public.settings USING btree (key, lang);


--
-- Name: signal_test_id_key; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX signal_test_id_key ON public.signal USING btree (test_id);


--
-- Name: statistik_austria_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX statistik_austria_gix ON public.statistik_austria_gem_20180101 USING gist (geom);


--
-- Name: test_client_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_client_id_idx ON public.test USING btree (client_id);


--
-- Name: test_deleted_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_deleted_idx ON public.test USING btree (deleted);


--
-- Name: test_developer_code_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_developer_code_idx ON public.test USING btree (developer_code);


--
-- Name: test_device_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_device_idx ON public.test USING btree (device);


--
-- Name: test_geo_accuracy_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_geo_accuracy_idx ON public.test USING btree (geo_accuracy);


--
-- Name: test_gkz_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_gkz_bev_idx ON public.test USING btree (gkz_bev);


--
-- Name: test_gkz_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_gkz_idx ON public.test USING btree (gkz);


--
-- Name: test_gkz_sa_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_gkz_sa_idx ON public.test USING btree (gkz_sa);


--
-- Name: test_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_idx ON public.test USING btree (((network_type <> ALL (ARRAY[0, 99]))));


--
-- Name: test_kg_nr_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_kg_nr_bev_idx ON public.test USING btree (kg_nr_bev);


--
-- Name: test_land_cover_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_land_cover_idx ON public.test USING btree (land_cover);


--
-- Name: test_mobile_network_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_mobile_network_id_idx ON public.test USING btree (mobile_network_id);


--
-- Name: test_mobile_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_mobile_provider_id_idx ON public.test USING btree (mobile_provider_id);


--
-- Name: test_ndt_test_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_ndt_test_id_idx ON public.test_ndt USING btree (test_id);


--
-- Name: test_network_operator_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_network_operator_idx ON public.test USING btree (network_operator);


--
-- Name: test_network_type_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_network_type_idx ON public.test USING btree (network_type);


--
-- Name: test_open_test_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_open_test_uuid_idx ON public.test USING btree (open_test_uuid);


--
-- Name: test_open_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_open_uuid_idx ON public.test USING btree (open_uuid);


--
-- Name: test_ping_median_log_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_ping_median_log_idx ON public.test USING btree (ping_median_log);


--
-- Name: test_ping_shortest_log_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_ping_shortest_log_idx ON public.test USING btree (ping_shortest_log);


--
-- Name: test_pinned_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_pinned_idx ON public.test USING btree (pinned);


--
-- Name: test_pinned_implausible_deleted_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_pinned_implausible_deleted_idx ON public.test USING btree (pinned, implausible, deleted);


--
-- Name: test_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_provider_id_idx ON public.test USING btree (provider_id);


--
-- Name: test_similar_test_uid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_similar_test_uid_idx ON public.test USING btree (similar_test_uid);


--
-- Name: test_speed_download_log_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_speed_download_log_idx ON public.test USING btree (speed_download_log);


--
-- Name: test_speed_upload_log_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_speed_upload_log_idx ON public.test USING btree (speed_upload_log);


--
-- Name: test_status_finished2_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_status_finished2_idx ON public.test USING btree ((((NOT deleted) AND (NOT implausible) AND ((status)::text = 'FINISHED'::text))), network_type);


--
-- Name: test_status_finished_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_status_finished_idx ON public.test USING btree ((((deleted = false) AND ((status)::text = 'FINISHED'::text))), network_type);


--
-- Name: test_status_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_status_idx ON public.test USING btree (status);


--
-- Name: test_test_slot_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_test_slot_idx ON public.test USING btree (test_slot);


--
-- Name: test_time_export; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_time_export ON public.test USING btree (date_part('month'::text, timezone('UTC'::text, "time")), date_part('year'::text, timezone('UTC'::text, "time")));


--
-- Name: test_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_time_idx ON public.test USING btree ("time");


--
-- Name: test_zip_code_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_zip_code_idx ON public.test USING btree (zip_code);


--
-- Name: test trigger_test; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_test BEFORE INSERT OR UPDATE ON public.test FOR EACH ROW EXECUTE PROCEDURE public.trigger_test();


--
-- Name: as2provider as2provider_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.as2provider
    ADD CONSTRAINT as2provider_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES public.provider(uid);


--
-- Name: cell_location cell_location_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cell_location
    ADD CONSTRAINT cell_location_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: client client_client_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_client_type_id_fkey FOREIGN KEY (client_type_id) REFERENCES public.client_type(uid);


--
-- Name: client client_sync_group_id; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.client
    ADD CONSTRAINT client_sync_group_id FOREIGN KEY (sync_group_id) REFERENCES public.sync_group(uid);


--
-- Name: geo_location location_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location
    ADD CONSTRAINT location_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: mccmnc2provider mccmnc2provider_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.mccmnc2provider
    ADD CONSTRAINT mccmnc2provider_provider_id_fkey FOREIGN KEY (provider_id) REFERENCES public.provider(uid);


--
-- Name: ping ping_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ping
    ADD CONSTRAINT ping_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: qos_test_result qos_test_result_qos_test_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result
    ADD CONSTRAINT qos_test_result_qos_test_uid_fkey FOREIGN KEY (qos_test_uid) REFERENCES public.qos_test_objective(uid) ON DELETE CASCADE;


--
-- Name: qos_test_result qos_test_result_test_uid; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result
    ADD CONSTRAINT qos_test_result_test_uid FOREIGN KEY (test_uid) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: signal signal_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal
    ADD CONSTRAINT signal_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: test test_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.client(uid) ON DELETE CASCADE;


--
-- Name: test_loopmode test_loopmode_test_client_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode
    ADD CONSTRAINT test_loopmode_test_client_uuid_fkey FOREIGN KEY (client_uuid) REFERENCES public.client(uuid);


--
-- Name: test_loopmode test_loopmode_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode
    ADD CONSTRAINT test_loopmode_test_uuid_fkey FOREIGN KEY (test_uuid) REFERENCES public.test(uuid);


--
-- Name: test test_mobile_provider_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_mobile_provider_id_fkey FOREIGN KEY (mobile_provider_id) REFERENCES public.provider(uid);


--
-- Name: test_ndt test_ndt_test_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_ndt
    ADD CONSTRAINT test_ndt_test_id_fkey FOREIGN KEY (test_id) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: test test_provider_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_provider_fkey FOREIGN KEY (provider_id) REFERENCES public.provider(uid);


--
-- Name: test test_test_server_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_test_server_id_fkey FOREIGN KEY (server_id) REFERENCES public.test_server(uid);


--
-- Name: TABLE device_map; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.device_map TO rmbt_group_read_only;


--
-- Name: TABLE as2provider; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.as2provider TO rmbt_group_read_only;


--
-- Name: TABLE cell_location; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.cell_location TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.cell_location TO rmbt_group_control;


--
-- Name: SEQUENCE cell_location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.cell_location_uid_seq TO rmbt_group_control;


--
-- Name: TABLE clc12_all_oesterreich; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.clc12_all_oesterreich TO rmbt_group_read_only;


--
-- Name: TABLE clc_legend; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.clc_legend TO rmbt_group_read_only;


--
-- Name: TABLE client; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.client TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.client TO rmbt_group_control;


--
-- Name: TABLE client_type; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.client_type TO rmbt_group_read_only;


--
-- Name: SEQUENCE client_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.client_uid_seq TO rmbt_group_control;


--
-- Name: TABLE geo_location; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.geo_location TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.geo_location TO rmbt_group_control;


--
-- Name: TABLE json_sender; Type: ACL; Schema: public; Owner: rmbt
--

GRANT UPDATE ON TABLE public.json_sender TO rmbt_group_control;


--
-- Name: TABLE kategorisierte_gemeinden; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.kategorisierte_gemeinden TO rmbt_group_read_only;


--
-- Name: SEQUENCE location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.location_uid_seq TO rmbt_group_control;


--
-- Name: TABLE logged_actions; Type: ACL; Schema: public; Owner: rmbt
--

GRANT INSERT ON TABLE public.logged_actions TO rmbt_group_control;


--
-- Name: TABLE mcc2country; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.mcc2country TO rmbt_group_read_only;


--
-- Name: TABLE mccmnc2name; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.mccmnc2name TO rmbt_group_read_only;
GRANT SELECT ON TABLE public.mccmnc2name TO rmbt_group_control;


--
-- Name: TABLE mccmnc2provider; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.mccmnc2provider TO rmbt_group_read_only;


--
-- Name: TABLE ne_10m_admin_0_countries; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.ne_10m_admin_0_countries TO rmbt_group_read_only;


--
-- Name: TABLE network_type; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.network_type TO rmbt_group_read_only;


--
-- Name: TABLE news; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.news TO rmbt_group_read_only;


--
-- Name: TABLE oesterreich_bev_kg_lam_mitattribute_2017_10_02; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.oesterreich_bev_kg_lam_mitattribute_2017_10_02 TO rmbt_group_read_only;


--
-- Name: TABLE ping; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.ping TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.ping TO rmbt_group_control;


--
-- Name: SEQUENCE ping_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.ping_uid_seq TO rmbt_group_control;


--
-- Name: TABLE provider; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.provider TO rmbt_group_read_only;


--
-- Name: TABLE qos_test_desc; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qos_test_desc TO rmbt_group_read_only;


--
-- Name: TABLE qos_test_objective; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qos_test_objective TO rmbt_group_read_only;


--
-- Name: TABLE qos_test_result; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qos_test_result TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.qos_test_result TO rmbt_group_control;


--
-- Name: SEQUENCE qos_test_result_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.qos_test_result_uid_seq TO rmbt_group_control;


--
-- Name: TABLE qos_test_type_desc; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qos_test_type_desc TO rmbt_group_read_only;


--
-- Name: TABLE radio_cell; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.radio_cell TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.radio_cell TO rmbt_group_control;


--
-- Name: SEQUENCE radio_cell_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.radio_cell_uid_seq TO rmbt_group_control;


--
-- Name: TABLE radio_signal; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.radio_signal TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.radio_signal TO rmbt_group_control;


--
-- Name: SEQUENCE radio_signal_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.radio_signal_uid_seq TO rmbt_group_control;


--
-- Name: TABLE settings; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.settings TO rmbt_group_read_only;


--
-- Name: TABLE signal; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.signal TO rmbt_group_read_only;
GRANT INSERT ON TABLE public.signal TO rmbt_group_control;


--
-- Name: SEQUENCE signal_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.signal_uid_seq TO rmbt_group_control;


--
-- Name: TABLE speed; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.speed TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.speed TO rmbt_group_control;


--
-- Name: TABLE statistik_austria_gem_20180101; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.statistik_austria_gem_20180101 TO rmbt_group_read_only;


--
-- Name: TABLE status; Type: ACL; Schema: public; Owner: rmbt
--

GRANT INSERT,UPDATE ON TABLE public.status TO rmbt_group_control;
GRANT SELECT ON TABLE public.status TO rmbt_group_read_only;


--
-- Name: SEQUENCE status_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT UPDATE ON SEQUENCE public.status_uid_seq TO rmbt_group_control;


--
-- Name: TABLE sync_group; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.sync_group TO rmbt_group_read_only;
GRANT INSERT,DELETE ON TABLE public.sync_group TO rmbt_group_control;


--
-- Name: SEQUENCE sync_group_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.sync_group_uid_seq TO rmbt_group_control;


--
-- Name: TABLE test; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.test TO rmbt_group_control;


--
-- Name: TABLE test_loopmode; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test_loopmode TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.test_loopmode TO rmbt_group_control;


--
-- Name: SEQUENCE test_loopmode_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.test_loopmode_uid_seq TO rmbt_group_control;


--
-- Name: TABLE test_ndt; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test_ndt TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.test_ndt TO rmbt_group_control;


--
-- Name: SEQUENCE test_ndt_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.test_ndt_uid_seq TO rmbt_group_control;


--
-- Name: TABLE test_server; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test_server TO rmbt_group_read_only;


--
-- Name: SEQUENCE test_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.test_uid_seq TO rmbt_group_control;


--
-- Name: TABLE v_dl_bandwidth_per_minute; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.v_dl_bandwidth_per_minute TO rmbt_group_read_only;


--
-- Name: TABLE v_get_replication_delay; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_get_replication_delay TO nagios;


--
-- Name: TABLE v_test; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_test TO rmbt_group_read_only;


--
-- Name: TABLE v_test2; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_test2 TO rmbt_group_read_only;
GRANT SELECT ON TABLE public.v_test2 TO rmbt;


--
-- Name: TABLE v_test3; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_test3 TO rmbt_group_read_only;
GRANT SELECT ON TABLE public.v_test3 TO rmbt;


--
-- PostgreSQL database dump complete
--

