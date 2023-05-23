--
-- PostgreSQL database dump
--

-- Dumped from database version 13.11 (Debian 13.11-0+deb11u1)
-- Dumped by pg_dump version 13.11 (Debian 13.11-0+deb11u1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: pg_cron; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_cron WITH SCHEMA public;


--
-- Name: EXTENSION pg_cron; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_cron IS 'Job scheduler for PostgreSQL';


--
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- Name: pgstattuple; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgstattuple WITH SCHEMA public;


--
-- Name: EXTENSION pgstattuple; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgstattuple IS 'show tuple-level statistics';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- Name: postgis_raster; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_raster WITH SCHEMA public;


--
-- Name: EXTENSION postgis_raster; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis_raster IS 'PostGIS raster types and functions';


--
-- Name: quantile; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS quantile WITH SCHEMA public;


--
-- Name: EXTENSION quantile; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION quantile IS 'Provides quantile aggregate function.';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
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
-- Name: fix_location(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fix_location(a integer, b integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    i     INTEGER := 0;
    done  INTEGER := 0;
    err   BOOLEAN;
    uuid  UUID;
    guuid UUID;
BEGIN
    raise notice 'v2';
    FOR i IN a..b
        LOOP
            BEGIN
                err := false;
                raise notice 'count= %',i;

                select into uuid open_test_uuid from fix_location where uid = i;
                raise notice 'uuid= %',uuid::text;
                select into guuid g.geo_location_uuid
                from geo_location g
                where open_test_uuid = uuid
                  and time_ns > (-20000000000)
                order by accuracy, time_ns asc
                limit 1;
                raise notice 'guuid= %',guuid::text;
                delete from test_location where open_test_uuid=uuid;
                if guuid is not null then

                    INSERT INTO test_location (open_test_uuid, 
                                               geo_long, 
                                               geo_lat, 
                                               location, 
                                               geo_accuracy, 
                                               geo_provider,
                                               geo_location_uuid)
                    select uuid,
                           g.geo_long,
                           g.geo_lat,
                           g.location,
                           g.accuracy,
                           g.provider,
                           guuid
                    from geo_location g
                    where g.geo_location_uuid = guuid
                    limit 1;
                END IF;
            END;

        END LOOP;
    RETURN done;
END;
$$;


ALTER FUNCTION public.fix_location(a integer, b integer) OWNER TO postgres;

--
-- Name: fix_location0(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fix_location0(a integer, b integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    i     INTEGER := 0;
    done  INTEGER := 0;
    err   BOOLEAN;
    uuid  UUID;
    guuid UUID;
BEGIN
    raise notice 'v2';
    FOR i IN a..b
        LOOP
            BEGIN
                err := false;
                raise notice 'count= %',i;

                select into uuid open_test_uuid
                from fix_location0
                where uid = i
                  and count_ns0 = 1;
                if uuid is not null then
                    raise notice 'uuid= %',uuid::text;
                    select into guuid g.geo_location_uuid
                    from geo_location g
                    where open_test_uuid = uuid
                      and time_ns > (-20000000000)
                      and time_ns != 0.0
                    limit 1;
                    raise notice 'guuid= %',guuid::text;
                    delete from test_location where open_test_uuid = uuid;
                    if guuid is not null then

                        INSERT INTO test_location (open_test_uuid,
                                                   geo_long,
                                                   geo_lat,
                                                   location,
                                                   geo_accuracy,
                                                   geo_provider,
                                                   geo_location_uuid)
                        select uuid,
                               g.geo_long,
                               g.geo_lat,
                               g.location,
                               g.accuracy,
                               g.provider,
                               guuid
                        from geo_location g
                        where g.geo_location_uuid = guuid
                        limit 1;
                    END IF;
                END IF;
            END;

        END LOOP;
    RETURN done;
END;
$$;


ALTER FUNCTION public.fix_location0(a integer, b integer) OWNER TO postgres;

--
-- Name: get_bev_vgd(public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_bev_vgd(location public.geometry) RETURNS TABLE(kg_nr character varying, kg_nr_bev integer, kg character varying, meridian character varying, gkz character varying, gkz_bev integer, pg character varying, bkz character varying, pb character varying, fa_nr character varying, fa character varying, gb_kz character varying, gb character varying, va_nr character varying, va character varying, bl_kz character varying, bl character varying, st_kz smallint, st character varying, fl double precision, geom public.geometry)
    LANGUAGE plpgsql
    AS $$

BEGIN


    BEGIN
        RETURN QUERY
            SELECT bev_vgd.kg_nr,
                   bev_vgd.kg_nr::integer,
                   bev_vgd.kg,
                   bev_vgd.meridian,
                   bev_vgd.gkz,
                   bev_vgd.gkz::integer,
                   bev_vgd.pg,
                   bev_vgd.bkz,
                   bev_vgd.pb,
                   bev_vgd.fa_nr,
                   bev_vgd.fa,
                   bev_vgd.gb_kz,
                   bev_vgd.gb,
                   bev_vgd.va_nr,
                   bev_vgd.va,
                   bev_vgd.bl_kz,
                   bev_vgd.bl,
                   bev_vgd.st_kz,
                   bev_vgd.st,
                   bev_vgd.fl,
                   bev_vgd.geom

            FROM bev_vgd
            WHERE within(st_transform(location, 31287), bev_vgd.geom)
            LIMIT 1;

    EXCEPTION
        WHEN undefined_table THEN
            -- just return NULL, but ignore missing database
            RAISE NOTICE '%', SQLERRM;
    END;

END;


$$;


ALTER FUNCTION public.get_bev_vgd(location public.geometry) OWNER TO postgres;

--
-- Name: get_gkz_sa(public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_gkz_sa(location public.geometry) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    gkz_sa INTEGER := NULL;
BEGIN
    IF (location IS NULL) THEN
        RETURN NULL;
    end if;
    BEGIN
        SELECT sa.id::INTEGER INTO gkz_sa
        FROM statistik_austria_gem sa
        WHERE within(st_transform(location, 31287), sa.geom)
        LIMIT 1;

    EXCEPTION
        WHEN undefined_table THEN
            -- just return NULL, but ignore missing database
            RAISE NOTICE '%', SQLERRM;
    END;
    RETURN gkz_sa;
END;

$$;


ALTER FUNCTION public.get_gkz_sa(location public.geometry) OWNER TO postgres;

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
-- Name: getnewsstatus(boolean, timestamp with time zone, timestamp with time zone); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.getnewsstatus(boolean, timestamp with time zone, timestamp with time zone) RETURNS character varying
    LANGUAGE plpgsql
    AS $_$
DECLARE
    active alias for $1;
    startDate alias for $2;
    endDate alias for $3;
    now       timestamp with time zone;
    isEnded   boolean;
    isStarted boolean;
BEGIN
    now := NOW();
    isEnded := endDate IS NOT NULL AND endDate < now;
    isStarted := startDate < now;

    RETURN CASE
               WHEN active AND NOT isEnded AND NOT isStarted
                   THEN 'SCHEDULED'
               WHEN active AND NOT isEnded AND isStarted
                   THEN 'PUBLISHED'
               WHEN active AND isEnded
                   THEN 'EXPIRED'
               ELSE 'DRAFT'
        END;
END;
$_$;


ALTER FUNCTION public.getnewsstatus(boolean, timestamp with time zone, timestamp with time zone) OWNER TO postgres;

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
-- Name: interpolate_radio_signal_location(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.interpolate_radio_signal_location(in_open_test_uuid uuid) RETURNS TABLE(out_last_signal_uuid uuid, out_last_radio_signal_uuid uuid, out_last_geo_location_uuid uuid, out_next_geo_location_uuid uuid, out_open_test_uuid uuid, out_interpolated_location public.geometry, out_time timestamp with time zone)
    LANGUAGE plpgsql
    AS $$
-- USAGE:
-- # for data migration:
--   insert into radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, time) select (interpolate_radio_signal_location (open_test_uuid)).* from test where open_test_uuid = '0583309a-1c36-4048-90d1-a777be8ef4fd' order by uid asc;
-- # with single open_test_uuid (e.g. in trigger or server code):
--   insert into radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, time) select (interpolate_radio_signal_location ('0583309a-1c36-4048-90d1-a777be8ef4fd')).* ;
-- # show only (e.g for debugging):
--   select (interpolate_radio_signal_location ('0583309a-1c36-4048-90d1-a777be8ef4fd')).*;
-- # generate test data:
--   select * from (
--   (select * from interpolate_radio_signal_location ('0583309a-1c36-4048-90d1-a777be8ef4fd') left join signal on signal_uuid=out_last_signal_uuid left join radio_signal on radio_signal_uuid = out_last_radio_signal_uuid left join geo_location on geo_location_uuid= out_last_geo_location_uuid)
--   union
--   (select * from interpolate_radio_signal_location ('d1fe403a-14b5-41a7-946a-251b1a5f49ce') left join signal on signal_uuid=out_last_signal_uuid left join radio_signal on radio_signal_uuid = out_last_radio_signal_uuid left join geo_location on geo_location_uuid= out_last_geo_location_uuid)
--   ) as foo order by out_open_test_uuid, out_time;
declare
  signal_rows_found bool;
  test_time timestamptz;
  ALLOWED_GEO_LOCATION_PROVIDER constant varchar := 'gps';
  MAX_INACCURACY constant double precision := 100.0;
  MIN_TIME_NS constant bigint := -1*60*1000000000::bigint; -- -1 minute  in nanoseconds
  MAX_TIME_NS constant bigint :=  4*60*60*1000000000::bigint; --  4 hours in nanoseconds
BEGIN
  --raise notice '%',  in_open_test_uuid; --debug, can be commented out
SELECT test.time INTO test_time FROM test WHERE open_test_uuid = in_open_test_uuid;
if not found then
  return; -- no timestamp found for this open_test_uuid, return nothing and exit
end if;
CREATE TEMP TABLE if not exists temp_radio_signal_location (
  -- inputs:
  signal_uuid uuid,
  radio_signal_uuid uuid,
  geo_location_uuid uuid,
  open_test_uuid uuid,
  time_ns bigint, -- this is also debug or auxillary output for. e.g. sorting purposes - equals to a coalesce of all time_ns from geo_location, signal and radio_signal tables
  signal_strength int, -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  location geometry,
  -- intermediate internal results:
  time_ns_a bigint,     --time of first interpolation point
  location_a geometry,  --and its location
  time_ns_b bigint,     --time of last interpolation point
  location_b geometry,  --and its location
  -- outputs:
  last_signal_uuid uuid,       --last non-null signal_uuid
  last_radio_signal_uuid uuid, --last non-null radio_signal_uuid
  last_geo_location_uuid uuid, --last non-null geo_location_uuid
  next_geo_location_uuid uuid, --next non-null geo_location_uuid
  interpolated_location geometry, --points from geo_location and interpolated points for signal rows
  last_signal_strength int,       --last non-null signal strength
  "time" timestamptz,             --timestamp with accuracy of microseconds, equals to test.time + time_ns
  -- internal:
  uid bigserial
  ) on commit drop;
truncate table temp_radio_signal_location;
insert into temp_radio_signal_location (/*signal_uuid=NULL, radio_signal_uuid=NULL,*/ geo_location_uuid, open_test_uuid, time_ns, location/*, signal_strength*/)
  select geo_location_uuid, geo_location.open_test_uuid, time_ns, location
  from geo_location
  where geo_location.open_test_uuid = in_open_test_uuid
    and location is not null and accuracy BETWEEN 0.0 AND MAX_INACCURACY AND provider = ALLOWED_GEO_LOCATION_PROVIDER -- consider only data with good accuracy
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS;        -- consider only plausible time_ns values
if not found then
  return; -- no location data found for this open_test_uuid, return nothing and exit
end if;
insert into temp_radio_signal_location (signal_uuid, /*radio_signal_uuid=NULL, geo_location_uuid=NULL,*/ open_test_uuid, time_ns /*,location=NULL*/, signal_strength)
  select signal_uuid, signal.open_test_uuid,time_ns, lte_rsrp -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  from signal
  where signal.open_test_uuid = in_open_test_uuid
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS;       -- consider only plausible time_ns values
signal_rows_found := found;
insert into temp_radio_signal_location (/*signal_uuid=NULL,*/ radio_signal_uuid, /*geo_location_uuid=NULL,*/ open_test_uuid, time_ns /*,location=NULL*/, signal_strength)
  select radio_signal_uuid, radio_signal.open_test_uuid, time_ns, lte_rsrp -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  from radio_signal
  join radio_cell on radio_signal.cell_uuid = radio_cell.uuid and registered and active  --for active cells only and the active SIM in case of dual SIMs
  where radio_signal.open_test_uuid = in_open_test_uuid
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS;        -- consider only plausible time_ns values
if (not signal_rows_found and not /*radio_signal_rows_*/found) then -- or (signal_rows_found and /*radio_signal_rows_*/found) == additionally both signal and radio_signal found - would be more restrictive
  return; -- no signal data found, return nothing and exit
end if;
-- do first the time ascending handling
declare -- default values are NULL
  cursor_asc cursor for select * from temp_radio_signal_location order by time_ns asc for update;
  row record;
  last_oldsig_uuid uuid;
  last_sig_uuid uuid;
  last_geo_uuid uuid;
  last_time_ns_a bigint;
  last_location_a geometry;
  last_sig_strength int;
begin
for row in cursor_asc
  LOOP
    <<get_missing_values_asc>>
    BEGIN
      if row.signal_uuid is not null and row.signal_uuid is distinct from last_oldsig_uuid then
        last_oldsig_uuid := row.signal_uuid;
      end if;
      if row.radio_signal_uuid is not null and row.radio_signal_uuid is distinct from last_sig_uuid then
        last_sig_uuid := row.radio_signal_uuid;
      end if;
      if row.geo_location_uuid is not null and row.geo_location_uuid is distinct from last_geo_uuid then
        last_geo_uuid := row.geo_location_uuid;
      end if;
      if row.time_ns is not null and row.time_ns is distinct from last_time_ns_a and row.geo_location_uuid is not null then
        last_time_ns_a := row.time_ns;
      end if;
      if row.location is not null and row.location is distinct from last_location_a then
        last_location_a := row.location;
      end if;
      if row.signal_strength is not null and row.signal_strength is distinct from last_sig_strength then
        last_sig_strength := row.signal_strength;
      end if;
    END get_missing_values_asc;
    <<populate_missing_values_asc>>
    BEGIN
      if last_oldsig_uuid is not null then -- assume last old signal
        update temp_radio_signal_location set last_signal_uuid = last_oldsig_uuid where current of cursor_asc;
      end if;
      if last_sig_uuid is not null then -- assume last signal
        update temp_radio_signal_location set last_radio_signal_uuid = last_sig_uuid where current of cursor_asc;
      end if;
      if last_geo_uuid is not null then -- assume last location
        update temp_radio_signal_location set last_geo_location_uuid = last_geo_uuid where current of cursor_asc;
      end if;
      if last_time_ns_a is not null then -- fill last time_ns_a
        update temp_radio_signal_location set time_ns_a = last_time_ns_a where current of cursor_asc;
      end if;
      if last_location_a is not null then -- assume last location
        update temp_radio_signal_location set location_a = last_location_a where current of cursor_asc;
      end if;
      if last_sig_strength is not null then -- assume last signal_strength
        update temp_radio_signal_location set last_signal_strength = last_sig_strength where current of cursor_asc;
      end if;
      update temp_radio_signal_location set time = test_time + ( (ROW.time_ns/1000.0) * INTERVAL '1 microsecond')  where current of cursor_asc;  -- timestamp has accuracy of microseconds
    END populate_missing_values_asc;
  END LOOP;
end;
-- secondly do the time descending handling
declare -- default values are NULL
  cursor_desc cursor for select * from temp_radio_signal_location order by time_ns desc for update;
  row record;
  next_geo_uuid uuid;
  last_time_ns_b bigint;
  last_location_b geometry;
  interpolated_line geometry;
  interpolated_point geometry;
  fraction double precision;
begin
for row in cursor_desc
  LOOP
    <<get_missing_values_desc>>
    BEGIN
      if row.geo_location_uuid is not null and row.geo_location_uuid is distinct from next_geo_uuid then
        next_geo_uuid := row.geo_location_uuid;
      end if;
      if row.time_ns is not null and row.time_ns is distinct from last_time_ns_b and row.geo_location_uuid is not null then
        last_time_ns_b := row.time_ns;
      end if;
      if row.location is not null and row.location is distinct from last_location_b then
        last_location_b := row.location;
      end if;
    END get_missing_values_desc;
    <<populate_missing_values_desc>>
    BEGIN
      if next_geo_uuid is not null then -- assume next location
        update temp_radio_signal_location set next_geo_location_uuid = next_geo_uuid where current of cursor_desc;
      end if;
      if last_time_ns_b is not null then -- fill next time_ns_b
        update temp_radio_signal_location set time_ns_b = last_time_ns_b where current of cursor_desc;
      end if;
      if last_location_b is not null then -- assume next location
        update temp_radio_signal_location set location_b = last_location_b where current of cursor_desc;
      end if;
    END populate_missing_values_desc;
  END LOOP;
end;
-- do the interpolation
declare -- default values are NULL
  cursor_asc cursor for select * from temp_radio_signal_location order by time_ns asc for update;
  row record;
  interpolated_line geometry;
  interpolated_point geometry;
  fraction double precision;
begin
for row in cursor_asc
  LOOP
    <<populate_interpolated_values>>
    BEGIN
      if row.location_a is not null then
          if row.location_b is not null then
            interpolated_line := ST_makeline(row.location_a, row.location_b);
            if row.time_ns_a <> row.time_ns_b then
              fraction := (row.time_ns - row.time_ns_a)::double precision/(row.time_ns_b - row.time_ns_a)::double precision;
            else
              fraction := 0; -- it is one point only
            end if;
            interpolated_point := ST_lineinterpolatepoint(interpolated_line, fraction);
            update temp_radio_signal_location set interpolated_location = interpolated_point /*, provider = 'interpolated'*/ where current of cursor_asc;
          else --row.location_b is null 
            -- do nothing
          end if;
      end if;
    END populate_interpolated_values;
  END LOOP;
end;
return query select
  last_signal_uuid,
  last_radio_signal_uuid,
  last_geo_location_uuid,
  next_geo_location_uuid,
  open_test_uuid,
  interpolated_location,
  "time"
  --debug:
  --,time_ns,
  --last_signal_strength
  --time_ns_a,
  --location_a,
  --time_ns_b,
  --location_b
  from temp_radio_signal_location
  where
    (((last_signal_uuid IS NOT NULL) AND (last_radio_signal_uuid IS NULL)) OR ((last_signal_uuid IS NULL) AND (last_radio_signal_uuid IS NOT NULL))) --only return rows with either signal or radio signal according to constraint xor_signals_not_null
    and  (((last_geo_location_uuid IS NOT NULL) AND (interpolated_location IS NOT NULL))) -- and with location according to constraint location_not_null_for_uuid
    AND  (last_geo_location_uuid <> next_geo_location_uuid) -- do not return not interpolated points
    AND "time" IS NOT NULL -- and with a timestamp
  order by time_ns asc;
-- do the approximate counting stuff
-- it is optional and can be commented out
/*<<do_the_counting>>
declare
  val bigint;
begin
  val := nextval('temp_radio_signal_location_uid_seq');
  --if val > 1 then
  --  perform setval('temp_radio_signal_location_uid_seq',val-1);
  --end if;
  raise notice 'nextval %', val;
end do_the_counting;*/
--DROP TABLE temp_radio_signal_location; --> leads to "out of shared memory" error and 100.000+ transaction locks
END;
$$;


ALTER FUNCTION public.interpolate_radio_signal_location(in_open_test_uuid uuid) OWNER TO rmbt;

--
-- Name: interpolate_radio_signal_location_v2(uuid); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.interpolate_radio_signal_location_v2(in_open_test_uuid uuid) RETURNS TABLE(out_last_signal_uuid uuid, out_last_radio_signal_uuid uuid, out_last_geo_location_uuid uuid, out_next_geo_location_uuid uuid, out_open_test_uuid uuid, out_interpolated_location public.geometry, out_time timestamp with time zone)
    LANGUAGE plpgsql
    SET client_min_messages TO 'warning'
    AS $$
-- v2: DJ 2022-03-03: this is the performance optimized interpolation function which interpolates from the last interpolated entry in the radio_signal_location table (if any)
-- v2: DJ 2022-03-03: tested and found to be equivalent to the previous version interpolate_radio_signal_location()
-- USAGE:
-- # for data migration:
--   insert into radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, time) select (interpolate_radio_signal_location_v2 (open_test_uuid)).* from test where open_test_uuid = '0583309a-1c36-4048-90d1-a777be8ef4fd' order by uid asc;
--   insert into radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, time) select (interpolate_radio_signal_location_v2 (open_test_uuid)).* from (select open_test_uuid from test where time between 'YYYY-MM-DD' and 'YYYY-MM-DD' order by uid asc) as foo;
-- # with single open_test_uuid (e.g. in trigger or server code):
--   insert into radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, time) select (interpolate_radio_signal_location_v2 ('0583309a-1c36-4048-90d1-a777be8ef4fd')).* ;
-- # show only (e.g for debugging):
--   select (interpolate_radio_signal_location_v2 ('0583309a-1c36-4048-90d1-a777be8ef4fd')).*;
-- # generate test data:
--   select * from (
--   (select * from interpolate_radio_signal_location_v2 ('0583309a-1c36-4048-90d1-a777be8ef4fd') left join signal on signal_uuid=out_last_signal_uuid left join radio_signal on radio_signal_uuid = out_last_radio_signal_uuid left join geo_location on geo_location_uuid= out_last_geo_location_uuid)
--   union
--   (select * from interpolate_radio_signal_location_v2 ('d1fe403a-14b5-41a7-946a-251b1a5f49ce') left join signal on signal_uuid=out_last_signal_uuid left join radio_signal on radio_signal_uuid = out_last_radio_signal_uuid left join geo_location on geo_location_uuid= out_last_geo_location_uuid)
--   ) as foo order by out_open_test_uuid, out_time;
declare
  signal_rows_found bool;
  test_time timestamptz;
  ALLOWED_GEO_LOCATION_PROVIDER constant varchar := 'gps';
  MAX_INACCURACY constant double precision := 100.0;
  MIN_TIME_NS constant bigint := -1*60*1000000000::bigint; -- -1 minute  in nanoseconds
  MAX_TIME_NS constant bigint :=  4*60*60*1000000000::bigint; --  4 hours in nanoseconds
  from_signal_uuid uuid; from_radio_signal_uuid uuid; from_geo_location_uuid uuid; --v2
  from_time_ns bigint := MIN_TIME_NS; --v2  
BEGIN
--raise notice '%',  in_open_test_uuid; --debug, can be commented out

--v2: check if there are interpolated items for this open_test_uuid
SELECT last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid 
  INTO from_signal_uuid, from_radio_signal_uuid, from_geo_location_uuid
  FROM radio_signal_location WHERE open_test_uuid = in_open_test_uuid ORDER BY uid DESC NULLS LAST LIMIT 1; --#1151#comment:64: NULLS LAST because of performance problems of LIMIT 1
if found THEN --v2: get the least interpolated time_ns from all
  SELECT COALESCE(MIN(time_ns), MIN_TIME_NS) --v2: In case of time_ns IS NULL take the default MIN_TIME_NS with COALESCE
    INTO from_time_ns FROM (
      (SELECT time_ns FROM signal WHERE signal_uuid = from_signal_uuid ORDER BY uid DESC LIMIT 1)
      UNION 
      (SELECT time_ns FROM radio_signal WHERE radio_signal_uuid = from_radio_signal_uuid ORDER BY uid DESC LIMIT 1)
      UNION 
      (SELECT time_ns FROM geo_location WHERE geo_location_uuid = from_geo_location_uuid ORDER BY uid DESC LIMIT 1)
      ) AS foo;
end if;
-- raise notice 'from_time_ns %',  from_time_ns; --debug, can be commented out

SELECT test.time INTO test_time FROM test WHERE open_test_uuid = in_open_test_uuid;
if not found then
  return; -- no timestamp found for this open_test_uuid, return nothing and exit
end if;
CREATE TEMP TABLE if not exists temp_radio_signal_location (
  -- inputs:
  signal_uuid uuid,
  radio_signal_uuid uuid,
  geo_location_uuid uuid,
  open_test_uuid uuid,
  time_ns bigint, -- this is also debug or auxillary output for. e.g. sorting purposes - equals to a coalesce of all time_ns from geo_location, signal and radio_signal tables
  signal_strength int, -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  location geometry,
  -- intermediate internal results:
  time_ns_a bigint,     --time of first interpolation point
  location_a geometry,  --and its location
  time_ns_b bigint,     --time of last interpolation point
  location_b geometry,  --and its location
  -- outputs:
  last_signal_uuid uuid,       --last non-null signal_uuid
  last_radio_signal_uuid uuid, --last non-null radio_signal_uuid
  last_geo_location_uuid uuid, --last non-null geo_location_uuid
  next_geo_location_uuid uuid, --next non-null geo_location_uuid
  interpolated_location geometry, --points from geo_location and interpolated points for signal rows
  last_signal_strength int,       --last non-null signal strength
  "time" timestamptz,             --timestamp with accuracy of microseconds, equals to test.time + time_ns
  -- internal:
  uid bigserial
  ) on commit drop;
truncate table temp_radio_signal_location;
insert into temp_radio_signal_location (/*signal_uuid=NULL, radio_signal_uuid=NULL,*/ geo_location_uuid, open_test_uuid, time_ns, location/*, signal_strength*/)
  select geo_location_uuid, geo_location.open_test_uuid, time_ns, geom4326 --was geo_location.location with srid 900913
  from geo_location
  where geo_location.open_test_uuid = in_open_test_uuid
    and geom4326 /*was geo_location.location with srid 900913*/ is not null and accuracy BETWEEN 0.0 AND MAX_INACCURACY AND provider = ALLOWED_GEO_LOCATION_PROVIDER -- consider only data with good accuracy
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS -- consider only plausible time_ns values
    AND time_ns >= from_time_ns; --v2: optimized from last interpolated time_ns
if not found then
  return; -- no location data found for this open_test_uuid, return nothing and exit
end if;
insert into temp_radio_signal_location (signal_uuid, /*radio_signal_uuid=NULL, geo_location_uuid=NULL,*/ open_test_uuid, time_ns /*,location=NULL*/, signal_strength)
  select signal_uuid, signal.open_test_uuid,time_ns, lte_rsrp -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  from signal
  where signal.open_test_uuid = in_open_test_uuid
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS -- consider only plausible time_ns values
    AND time_ns >= from_time_ns; --v2: optimized from last interpolated time_ns
signal_rows_found := found;
insert into temp_radio_signal_location (/*signal_uuid=NULL,*/ radio_signal_uuid, /*geo_location_uuid=NULL,*/ open_test_uuid, time_ns /*,location=NULL*/, signal_strength)
  select radio_signal_uuid, radio_signal.open_test_uuid, time_ns, lte_rsrp -- not used, currently only 4G, should be a coalesce of all technologies - TODO tbd
  from radio_signal
  join radio_cell on radio_signal.cell_uuid = radio_cell.uuid and registered and active  --for active cells only and the active SIM in case of dual SIMs
  where radio_signal.open_test_uuid = in_open_test_uuid
    AND time_ns BETWEEN MIN_TIME_NS AND MAX_TIME_NS -- consider only plausible time_ns values
    AND time_ns >= from_time_ns; --v2: optimized from last interpolated time_ns
if (not signal_rows_found and not /*radio_signal_rows_*/found) then -- or (signal_rows_found and /*radio_signal_rows_*/found) == additionally both signal and radio_signal found - would be more restrictive
  return; -- no signal data found, return nothing and exit
end if;
-- do first the time ascending handling
declare -- default values are NULL
  cursor_asc cursor for select * from temp_radio_signal_location order by time_ns asc for update;
  row record;
  last_oldsig_uuid uuid;
  last_sig_uuid uuid;
  last_geo_uuid uuid;
  last_time_ns_a bigint;
  last_location_a geometry;
  last_sig_strength int;
begin
for row in cursor_asc
  LOOP
    <<get_missing_values_asc>>
    BEGIN
      if row.signal_uuid is not null and row.signal_uuid is distinct from last_oldsig_uuid then
        last_oldsig_uuid := row.signal_uuid;
      end if;
      if row.radio_signal_uuid is not null and row.radio_signal_uuid is distinct from last_sig_uuid then
        last_sig_uuid := row.radio_signal_uuid;
      end if;
      if row.geo_location_uuid is not null and row.geo_location_uuid is distinct from last_geo_uuid then
        last_geo_uuid := row.geo_location_uuid;
      end if;
      if row.time_ns is not null and row.time_ns is distinct from last_time_ns_a and row.geo_location_uuid is not null then
        last_time_ns_a := row.time_ns;
      end if;
      if row.location is not null and row.location is distinct from last_location_a then
        last_location_a := row.location;
      end if;
      if row.signal_strength is not null and row.signal_strength is distinct from last_sig_strength then
        last_sig_strength := row.signal_strength;
      end if;
    END get_missing_values_asc;
    <<populate_missing_values_asc>>
    BEGIN
      if last_oldsig_uuid is not null then -- assume last old signal
        update temp_radio_signal_location set last_signal_uuid = last_oldsig_uuid where current of cursor_asc;
      end if;
      if last_sig_uuid is not null then -- assume last signal
        update temp_radio_signal_location set last_radio_signal_uuid = last_sig_uuid where current of cursor_asc;
      end if;
      if last_geo_uuid is not null then -- assume last location
        update temp_radio_signal_location set last_geo_location_uuid = last_geo_uuid where current of cursor_asc;
      end if;
      if last_time_ns_a is not null then -- fill last time_ns_a
        update temp_radio_signal_location set time_ns_a = last_time_ns_a where current of cursor_asc;
      end if;
      if last_location_a is not null then -- assume last location
        update temp_radio_signal_location set location_a = last_location_a where current of cursor_asc;
      end if;
      if last_sig_strength is not null then -- assume last signal_strength
        update temp_radio_signal_location set last_signal_strength = last_sig_strength where current of cursor_asc;
      end if;
      update temp_radio_signal_location set time = test_time + ( (ROW.time_ns/1000.0) * INTERVAL '1 microsecond')  where current of cursor_asc;  -- timestamp has accuracy of microseconds
    END populate_missing_values_asc;
  END LOOP;
end;
-- secondly do the time descending handling
declare -- default values are NULL
  cursor_desc cursor for select * from temp_radio_signal_location order by time_ns desc for update;
  row record;
  next_geo_uuid uuid;
  last_time_ns_b bigint;
  last_location_b geometry;
  interpolated_line geometry;
  interpolated_point geometry;
  fraction double precision;
begin
for row in cursor_desc
  LOOP
    <<get_missing_values_desc>>
    BEGIN
      if row.geo_location_uuid is not null and row.geo_location_uuid is distinct from next_geo_uuid then
        next_geo_uuid := row.geo_location_uuid;
      end if;
      if row.time_ns is not null and row.time_ns is distinct from last_time_ns_b and row.geo_location_uuid is not null then
        last_time_ns_b := row.time_ns;
      end if;
      if row.location is not null and row.location is distinct from last_location_b then
        last_location_b := row.location;
      end if;
    END get_missing_values_desc;
    <<populate_missing_values_desc>>
    BEGIN
      if next_geo_uuid is not null then -- assume next location
        update temp_radio_signal_location set next_geo_location_uuid = next_geo_uuid where current of cursor_desc;
      end if;
      if last_time_ns_b is not null then -- fill next time_ns_b
        update temp_radio_signal_location set time_ns_b = last_time_ns_b where current of cursor_desc;
      end if;
      if last_location_b is not null then -- assume next location
        update temp_radio_signal_location set location_b = last_location_b where current of cursor_desc;
      end if;
    END populate_missing_values_desc;
  END LOOP;
end;
-- do the interpolation
declare -- default values are NULL
  cursor_asc cursor for select * from temp_radio_signal_location order by time_ns asc for update;
  row record;
  interpolated_line geometry;
  interpolated_point geometry;
  fraction double precision;
begin
for row in cursor_asc
  LOOP
    <<populate_interpolated_values>>
    BEGIN
      if row.location_a is not null then
          if row.location_b is not null then
            interpolated_line := ST_makeline(row.location_a, row.location_b);
            if row.time_ns_a <> row.time_ns_b then
              fraction := (row.time_ns - row.time_ns_a)::double precision/(row.time_ns_b - row.time_ns_a)::double precision;
            else
              fraction := 0; -- it is one point only
            end if;
            interpolated_point := ST_lineinterpolatepoint(interpolated_line, fraction);
            update temp_radio_signal_location set interpolated_location = interpolated_point /*, provider = 'interpolated'*/ where current of cursor_asc;
          else --row.location_b is null 
            -- do nothing
          end if;
      end if;
    END populate_interpolated_values;
  END LOOP;
end;
return query select
  last_signal_uuid,
  last_radio_signal_uuid,
  last_geo_location_uuid,
  next_geo_location_uuid,
  open_test_uuid,
  interpolated_location,
  "time"
  --debug:
  --,time_ns,
  --last_signal_strength
  --time_ns_a,
  --location_a,
  --time_ns_b,
  --location_b
  from temp_radio_signal_location
  where
    (((last_signal_uuid IS NOT NULL) AND (last_radio_signal_uuid IS NULL)) OR ((last_signal_uuid IS NULL) AND (last_radio_signal_uuid IS NOT NULL))) --only return rows with either signal or radio signal according to constraint xor_signals_not_null
    and  (((last_geo_location_uuid IS NOT NULL) AND (interpolated_location IS NOT NULL))) -- and with location according to constraint location_not_null_for_uuid
    AND  (last_geo_location_uuid <> next_geo_location_uuid) -- do not return not interpolated points
    AND "time" IS NOT NULL -- and with a timestamp
  order by time_ns asc;
-- do the approximate counting stuff
-- it is optional and can be commented out
/*<<do_the_counting>>
declare
  val bigint;
begin
  val := nextval('temp_radio_signal_location_uid_seq');
  --if val > 1 then
  --  perform setval('temp_radio_signal_location_uid_seq',val-1);
  --end if;
  raise notice 'nextval %', val;
end do_the_counting;*/
--DROP TABLE temp_radio_signal_location; --> leads to "out of shared memory" error and 100.000+ transaction locks
END;
$$;


ALTER FUNCTION public.interpolate_radio_signal_location_v2(in_open_test_uuid uuid) OWNER TO rmbt;

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
-- Name: rmbt_get_country_iso_a2(public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rmbt_get_country_iso_a2(point public.geometry) RETURNS character varying
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
DECLARE
  -- ISO3266 two digit country code (e.g. 'US')
  a2 varchar(5);
BEGIN

	-- Example query: select rmbt_get_country_iso_a2(st_setsrid(ST_GeomFromText('POINT(-71.064544 42.28787)'),4326));

	
	select into a2 ac.iso_a2 from admin_0_countries ac where point && ac.geom and within(point,ac.geom) and char_length(iso_a2) = 2;
    return a2;
    
END;
$$;


ALTER FUNCTION public.rmbt_get_country_iso_a2(point public.geometry) OWNER TO postgres;

--
-- Name: rmbt_get_distance_iso_a2(public.geometry, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rmbt_get_distance_iso_a2(point public.geometry, a2 character varying) RETURNS double precision
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $$
DECLARE
  -- ISO3266 two digit country code (e.g. 'US')
  distance float;
BEGIN
    -- returns the distance in meter (m) betweeen location in WGS84 (EPSG:4236) and a two digit country code (e.g. 'US')
	
	-- Example query: select rmbt_get_distance_iso_a2(st_setsrid(ST_GeomFromText('POINT(-71.064544 42.28787)'),4326),'CA');
 		
	return  ST_DistanceSpheroid(point,(select geom from admin_0_countries ac where iso_a2=a2),'SPHEROID["WGS 84",6378137,298.257223563]');
    
END;
$$;


ALTER FUNCTION public.rmbt_get_distance_iso_a2(point public.geometry, a2 character varying) OWNER TO postgres;

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
-- Name: rmbt_lte_rsrp(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rmbt_lte_rsrp(otu uuid) RETURNS integer
    LANGUAGE plpgsql
    AS $$
   declare
      rsrp int4;

	BEGIN

   select min(rs.lte_rsrp) into rsrp from radio_signal rs 
left join radio_cell rc on rc.uuid  = rs.cell_uuid 
where rs.open_test_uuid = otu  and rc.active = true and (rc.primary_data_subscription is null or rc.primary_data_subscription ='true') group by rs.open_test_uuid;

if (rsrp is null) then
   select min(s.lte_rsrp) into rsrp from signal s where s.open_test_uuid = otu; 
  end if;
   
    return rsrp;
	END;
$$;


ALTER FUNCTION public.rmbt_lte_rsrp(otu uuid) OWNER TO postgres;

--
-- Name: rmbt_lte_rsrq(uuid); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rmbt_lte_rsrq(otu uuid) RETURNS integer
    LANGUAGE plpgsql
    AS $$
   declare
      rsrq int4;

	BEGIN

   select min(rs.lte_rsrq) into rsrq from radio_signal rs 
left join radio_cell rc on rc.uuid  = rs.cell_uuid 
where rs.open_test_uuid = otu  and rc.active = true and (rc.primary_data_subscription is null or rc.primary_data_subscription ='true') group by rs.open_test_uuid;

if (rsrq is null) then
   select min(s.lte_rsrq) into rsrq from signal s where s.open_test_uuid = otu; 
  end if;
   
    return rsrq;
	END;
$$;


ALTER FUNCTION public.rmbt_lte_rsrq(otu uuid) OWNER TO postgres;

--
-- Name: rmbt_purge_obsolete(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.rmbt_purge_obsolete(age integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  num integer;
	BEGIN
    if (age is null or age < 90) then
      return null;
	end if;

-- function purges IP etc for all tests older than age days
-- returns number of tests purged or NULL when age is less than 90 days

UPDATE test 
SET client_public_ip = NULL, public_ip_rdns = NULL, source_ip = NULL, 
 client_ip_local = NULL, wifi_bssid = NULL, wifi_ssid = NULL
WHERE (now()::date - time::date) > age
 AND (client_public_ip IS NOT NULL OR public_ip_rdns IS NOT NULL OR source_ip IS NOT NULL OR    
 client_ip_local IS NOT NULL OR wifi_bssid IS NOT NULL OR wifi_ssid IS NOT NULL);

GET diagnostics num = ROW_COUNT;

RAISE NOTICE 'rmbt_purge_obsolete for last % days: % rows purged', age, num ;

 return num;
		
	END;
$$;


ALTER FUNCTION public.rmbt_purge_obsolete(age integer) OWNER TO postgres;

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
-- Name: trigger_geo_location(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_geo_location() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	BEGIN

		
    IF (TG_OP = 'INSERT' and new.location is not NULL) then
       new.geom3857=st_setsrid(new.location,3857);
       new.geom4326=st_transform(new.geom3857,4326);
     end if;
    RETURN NEW;

	END;
$$;


ALTER FUNCTION public.trigger_geo_location() OWNER TO postgres;

--
-- Name: trigger_qos_test_result(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.trigger_qos_test_result() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
--
BEGIN
  IF  (TG_OP = 'INSERT' OR TG_OP = 'UPDATE')
  -- timeout is reported although the duration is shorter than the preconfigured one
  AND ((NEW."result" ->> 'duration_ns')::bigint < (NEW."result" ->> 'dns_objective_timeout')::bigint)
  AND ((NEW."result" ->> 'dns_result_info') = 'TIMEOUT'::TEXT) THEN
    NEW.deleted = TRUE;
  END IF;
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.trigger_qos_test_result() OWNER TO rmbt;

--
-- Name: trigger_radio_cell(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_radio_cell() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE

    _tmp_id integer;

BEGIN

    -- post process if location is updated
    IF (TG_OP = 'INSERT') then
        -- ignore Android invalid value
        if (new.location_id = 2147483647) then
           new.location_id = null;
        end if;
       -- ignore Android invalid value 
       if (new.area_code = 2147483647) then
           new.area_code = null;
        end if;  
       -- workaround for 3.x apps which swap area_code and location_id
       if (new.area_code > 65535) then
          _tmp_id = new.area_code;
          new.area_code = new.location_id;
          new.location_id = _tmp_id;
       end if;  
    end if;
    RETURN NEW;


END;
$$;


ALTER FUNCTION public.trigger_radio_cell() OWNER TO postgres;

--
-- Name: trigger_test(); Type: FUNCTION; Schema: public; Owner: rmbt
--

CREATE FUNCTION public.trigger_test() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    _country_location      varchar;
    _tmp_uuid              uuid;
    _tmp_uid               integer;
    _tmp_time              timestamp;
    _mcc_sim               VARCHAR;
    _mcc_net               VARCHAR;
    -- limit for accurate location (differs from map where 2000m and 10000m are thresholds)
    _min_accuracy CONSTANT integer := 3000;
    _tmp_geom4326          geometry;
    _tmp_cell_identifier    integer;

begin
	
	
	 -- workaround against deletion of client_software_version update
    IF (TG_OP = 'UPDATE' and new.client_software_version is null ) THEN
       NEW.client_software_version = old.client_software_version;
    END IF;
	
		 -- log if status changes from FINISED to something else
    IF ((TG_OP = 'UPDATE') and (new.status is distinct from old.status) and old.status = 'FINISED' ) THEN
       NEW.comment = 'Status finished modified';
       NEW.implausible = true;
    END IF;
	
	
                  -- #715 workaround for bug in old clients
    	-- swap cell_area_code,cell_location_id for Android version 3.x.x clients
	if (TG_OP = 'UPDATE' and  
	     new.plattform = 'Android' and
	     substring(new.client_software_version,1,1)='3' and 
	     old.cell_area_code is null and old.cell_location_id is null and
	     new.cell_area_code is not null and new.cell_location_id is not null and 
	     -- if identifiers are swapped, cell_locatio_id must be smaller than 2^16+1
	     new.cell_location_id < 65536) then 
        _tmp_cell_identifier = new.cell_location_id;
        new.cell_location_id = new.cell_area_code;
        new.cell_area_code = _tmp_cell_identifier;
	end if;     





    -- calc logarithmic speed downlink
    IF ((TG_OP = 'INSERT' OR NEW.speed_download IS DISTINCT FROM OLD.speed_download) AND NEW.speed_download > 0) THEN
        NEW.speed_download_log = (log(NEW.speed_download::double precision / 10)) / 4;
    END IF;
    -- calc logarithmic speed uplink
    IF ((TG_OP = 'INSERT' OR NEW.speed_upload IS DISTINCT FROM OLD.speed_upload) AND NEW.speed_upload > 0) THEN
        NEW.speed_upload_log = (log(NEW.speed_upload::double precision / 10)) / 4;
    END IF;
    -- calc logarithmic ping
    -- ping_shortest is obsolete
    IF ((TG_OP = 'INSERT' OR NEW.ping_shortest IS DISTINCT FROM OLD.ping_shortest) AND NEW.ping_shortest > 0) THEN
        NEW.ping_shortest_log = (log(NEW.ping_shortest::double precision / 1000000)) / 3;
        -- ping_median (from ping table)
        SELECT INTO NEW.ping_median floor(median(coalesce(value_server, value))) FROM ping WHERE NEW.uid = test_id;
        NEW.ping_median_log = (log(NEW.ping_median::double precision / 1000000)) / 3;
        IF (NEW.ping_median IS NULL) THEN
            NEW.ping_median = NEW.ping_shortest;
        END IF;
    END IF;
 
  -- migration to "clean" location projections:
 -- DZ 2023-04-02 now in ControlServer  
 --  IF ((NEW.location IS NOT NULL) AND (new.location is distinct from old.location)) THEN
 --        new.geom3857=st_setsrid(new.location,3857); 
 --       new.geom4326=st_transform(new.geom3857,4326);
 -- end if;     
   
   --  process location in table test_location

    IF ((NEW.geom4326 IS NOT NULL) AND (new.geom4326 is distinct from old.geom4326) AND
        (NEW.geo_location_uuid IS NOT NULL) ) THEN
        UPDATE test_location
        SET geo_location_uuid = NEW.geo_location_uuid,
            geom4326          = new.geom4326,
            -- geom3857 might be removed in the future from test_location
            geom3857	      = new.geom3857,
            -- location is obsolete and shall be removed from test_location when migration is finished
            location          = NEW.location,
            geo_lat           = NEW.geo_lat,
            geo_long          = NEW.geo_long,
            geo_accuracy      = NEW.geo_accuracy,
            geo_provider      = NEW.geo_provider
        WHERE open_test_uuid  = NEW.open_test_uuid;
        IF NOT FOUND THEN
            INSERT INTO test_location (geo_location_uuid,open_test_uuid, geom4326, geom3857, location, geo_lat,
                                       geo_long, geo_accuracy, geo_provider)
            VALUES (NEW.geo_location_uuid,NEW.open_test_uuid, new.geom4326, new.geom3857, NEW.location, NEW.geo_lat,
                    NEW.geo_long, NEW.geo_accuracy, NEW.geo_provider);
        END IF;
    END IF;

    select into _country_location country_location from test_location tl where tl.open_test_uuid = NEW.open_test_uuid;

    -- end of location post processing

    -- set roaming_type /mobile_provider_id
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
                -- TODO not correct for India - #1050 (old)
                IF (_mcc_sim = _mcc_net) THEN
                    NEW.roaming_type = 1; -- national roaming
                ELSE
                    NEW.roaming_type = 2; -- international roaming
                END IF;
            END IF;
        END IF;

        -- set mobile_provider_id
        -- do not set if outside Austria
        IF ((NEW.roaming_type IS NULL AND _country_location IS DISTINCT FROM 'AT') OR
            NEW.roaming_type IS NOT DISTINCT FROM 2) THEN -- not for foreign networks #659 
            NEW.mobile_provider_id = NULL;
        ELSE
            SELECT INTO NEW.mobile_provider_id provider_id
            FROM mccmnc2provider
            WHERE mcc_mnc_sim = NEW.network_sim_operator
              AND (valid_from IS NULL OR valid_from <= NEW.time)
              AND (valid_to IS NULL OR valid_to >= NEW.time)
              AND (mcc_mnc_network IS NULL OR mcc_mnc_network = NEW.network_operator)
            ORDER BY mcc_mnc_network NULLS LAST
            LIMIT 1;
        END IF;
    END IF;
    -- end of network_sim_operator

    -- set mobile_provider_id (again?)
    IF ((TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED')
        AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN -- update only new entries, skip old entries
        IF (NEW.network_operator is not NULL) THEN
            SELECT INTO NEW.mobile_network_id COALESCE(n.mapped_uid, n.uid)
            FROM mccmnc2name n
            WHERE NEW.network_operator = n.mccmnc
              AND (n.valid_from is null OR n.valid_from <= NEW.time)
              AND (n.valid_to is null or n.valid_to >= NEW.time)
              AND use_for_network = TRUE
            ORDER BY n.uid NULLS LAST
            LIMIT 1;
        END IF;

        -- set network_sim_operator
        IF (NEW.network_sim_operator is not NULL) THEN
            SELECT INTO NEW.mobile_sim_id COALESCE(n.mapped_uid, n.uid)
            FROM mccmnc2name n
            WHERE NEW.network_sim_operator = n.mccmnc
              AND (n.valid_from is null OR n.valid_from <= NEW.time)
              AND (n.valid_to is null or n.valid_to >= NEW.time)
              AND (NEW.network_sim_operator = n.mcc_mnc_network_mapping OR n.mcc_mnc_network_mapping is NULL)
              AND use_for_sim = TRUE
            ORDER BY n.uid NULLS LAST
            LIMIT 1;
        END IF;

    END IF;
    -- end of mobile_provider_id (again?)

    -- ignore automated tests from CLI
    IF ((TG_OP = 'UPDATE') AND (NEW.time > (now() - INTERVAL '5 minutes')) AND NEW.network_type = 97/*CLI*/ AND
        NEW.deleted = FALSE) THEN
        NEW.deleted = TRUE;
        NEW.comment = 'Exclude CLI per #211';
    END IF;

    -- ignore location of Samsung Galaxy Note 3
    -- TODO: should be done before post-processing of location, obsolete
    IF ((TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED')
        AND (NEW.time > (now() - INTERVAL '5 minutes'))
        AND NEW.model = 'SM-N9005'
        AND NEW.geo_provider = 'network') THEN
        NEW.geo_accuracy = 99999;
    END IF;

    -- plausibility check on distance from previous test
    IF ((TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED')
        AND (NEW.time > (now() - INTERVAL '5 minutes'))
        AND NEW.geo_accuracy is not null
        AND NEW.geo_accuracy <= 10000) THEN

        SELECT INTO _tmp_uid uid
        FROM test
        WHERE client_id = NEW.client_id
          AND time < NEW.time -- #668 allow only past tests
          AND (NEW.time - INTERVAL '24 hours' < time)
          AND geo_accuracy is not null
          AND geo_accuracy <= 10000
        ORDER BY uid DESC
        LIMIT 1;

        IF _tmp_uid is not null THEN
            SELECT INTO NEW.dist_prev ST_DistanceSpheroid(t.geom4326,new.geom4326,'SPHEROID["WGS 84",6378137,298.257223563]')
             -- #668 improve geo precision for the calculation of the distance (in meters) to a previous test      
            FROM test t
            WHERE uid = _tmp_uid;
            IF NEW.dist_prev is not null THEN
                SELECT INTO _tmp_time time
                FROM test t
                WHERE uid = _tmp_uid;
                NEW.speed_prev = NEW.dist_prev / GREATEST(0.000001, EXTRACT(EPOCH FROM (NEW.time - _tmp_time))) *
                                 3.6; -- #668 speed in km/h and don't allow division by zero
            END IF;
        END IF;
    END IF;
    -- end of plausibility check

    -- set network_type
    IF ((NEW.network_type > 0) AND (NEW.time > (now() - INTERVAL '5 minutes'))) THEN
        SELECT INTO NEW.network_group_name group_name
        FROM network_type
        WHERE uid = NEW.network_type
        LIMIT 1;
        SELECT INTO NEW.network_group_type type
        FROM network_type
        WHERE uid = NEW.network_type
        LIMIT 1;
    END IF;

    -- set open_uuid
    -- #759 Finalisation loop mode
    IF (TG_OP = 'UPDATE' AND OLD.status = 'STARTED' AND NEW.status = 'FINISHED')
        -- disabled due to #1540: AND (NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
    THEN
        _tmp_uuid = NULL;
        _tmp_geom4326 = NULL;
        SELECT open_uuid, geom4326 INTO _tmp_uuid, _tmp_geom4326
        FROM test -- find the open_uuid and geom4326
        WHERE (NEW.client_id = client_id)                                      -- of the current client
          AND (NEW.time > time)                                                -- thereby skipping the current entry (was: OLD.uid != uid)
          AND status = 'FINISHED'                                              -- of successful tests
          AND (NEW.time - time) < '4 hours'::INTERVAL                          -- within last 4 hours
          AND (NEW.time::DATE = time::DATE)                                    -- on the same day
          AND (NEW.network_group_type IS NOT DISTINCT FROM network_group_type) -- of the same technology (i.e. MOBILE, WLAN, LAN, CLI, NULL) - was: network_group_name
          AND (NEW.public_ip_asn IS NOT DISTINCT FROM public_ip_asn)           -- and of the same operator (including NULL)
        ORDER BY time DESC
        LIMIT 1; -- get only the latest test

        IF
                (_tmp_uuid IS NULL) -- previous query doesn't return any test
                OR -- OR
                (NEW.geom4326 IS NOT NULL AND _tmp_geom4326 IS NOT NULL
                    AND 
                    ST_DistanceSpheroid(new.geom4326,_tmp_geom4326,'SPHEROID["WGS 84",6378137,298.257223563]') 
                    >= 100) -- the distance to the last test >= 100m
        THEN
            _tmp_uuid = uuid_generate_v4(); --generate new open_uuid
        END IF;
        NEW.open_uuid = _tmp_uuid;
    END IF;
    --end of set open_uuid

    -- plausibility check on movement during test
    IF (TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED') THEN
        NEW.timestamp = now();

        SELECT INTO NEW.location_max_distance round(
                                                      ST_Distance( -- #668 improve geo precision for the calculation of the diagonal length (in meters) of the bounding box of one test
                                                              ST_SetSRID(ST_MakePoint(
                                                                                 ST_XMin(ST_Extent(ST_Transform(location, 4326))),
                                                                                 ST_YMin(ST_Extent(ST_Transform(location, 4326)))),
                                                                         4326)::geography,
                                                              ST_SetSRID(ST_MakePoint(
                                                                                 ST_XMax(ST_Extent(ST_Transform(location, 4326))),
                                                                                 ST_YMax(ST_Extent(ST_Transform(location, 4326)))),
                                                                         4326)::geography)
                                                  )
        FROM geo_location
        WHERE test_id = NEW.uid;
    END IF;

    -- plausibility check - Austrian networks outside Austria are not allowed #272
    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
        AND (
            (NEW.network_operator ILIKE '232%') -- test with Austrian mobile network operator
            )
        AND rmbt_get_distance_iso_a2(new.geom4326,'AT') > 35000 -- location is more than 35 km outside of the Austria shape
        ) 
    THEN
        NEW.status = 'UPDATE ERROR'; NEW.comment = 'Invalid location #272';
    END IF;

    -- ignore provider_id if location outside Austria
    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
        AND NEW.network_type in (97, 98, 99, 106, 107) -- CLI, LAN, WLAN, Ethernet, Bluetooth
        AND (
            (NEW.provider_id IS NOT NULL) -- Austrian operator
            )
        AND rmbt_get_distance_iso_a2(new.geom4326,'AT') > 3000 -- location is outside of the Austria shape with a tolerance of +3 km
        ) -- if
    -- TODO Do we really need such a long comment within the database here?
    THEN
        NEW.provider_id = NULL;
        NEW.comment = concat(
                'Not AT, no provider_id #664; ',
                NEW.comment, NULLIF(OLD.comment, NEW.comment));
    END IF;

    -- ignore tests with model name 'unknown'
    -- TODO justified/relevant?
    IF ((NEW.time > (now() - INTERVAL '5 minutes')) -- update only new entries, skip old entries
        AND (NEW.model = 'unknown') -- model is 'unknown'
        )
    THEN
        NEW.status = 'UPDATE ERROR'; NEW.comment = 'Unknown model #356';
    END IF;

    -- implement test pinning (tests excluded from statistics)
    IF ((TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED')
        AND (NEW.time > (now() - INTERVAL '5 minutes'))) -- update only new entries, skip old entries
    THEN -- Returns the uid of a previous similar test, otherwise -1. Also IF similar_test_uid = -1 then pinned = TRUE ELSE pinned=FALSE. Column similar_test_uid has a default value NULL, meaning the evaluation for similar test(s) wasn't performed yet.
        SELECT INTO NEW.similar_test_uid uid
        FROM test
        WHERE (similar_test_uid = -1 OR similar_test_uid IS NULL) -- consider only unsimilar or not yet evaluated tests
          AND NEW.open_uuid = open_uuid                           -- with the same open_uuid
          AND NEW.time > time
          AND (NEW.time - time) < '4 hours'::INTERVAL             -- in the last 4 hours
          AND NEW.public_ip_asn = public_ip_asn                   -- from the same network based on AS
          AND NEW.network_type = network_type                     -- of the same network_type
          AND CASE
                  WHEN (NEW.geom4326 IS NOT NULL AND NEW.geo_accuracy IS NOT NULL AND NEW.geo_accuracy < 2000
                      AND geom4326 IS NOT NULL AND geo_accuracy IS NOT NULL AND geo_accuracy < 2000)
                      THEN ST_DistanceSpheroid(new.geom4326,geom4326,'SPHEROID["WGS 84",6378137,298.257223563]')
                               < GREATEST(100, NEW.geo_accuracy) -- either within a radius of 100 m
                  ELSE TRUE -- or if no or inaccurate location, only other criteria count
            END
        ORDER BY time DESC -- consider the last, most previous test
        LIMIT 1;
        IF NEW.similar_test_uid IS NULL -- no similar test found
        then
            NEW.similar_test_uid = -1; -- indicate that we have searched for a similar test but nothing found
            NEW.pinned = TRUE; -- and set the pinned for the statistics
        ELSE
            NEW.pinned = FALSE; -- else in similar_test_uid the uid of a previous test is stored so the test shouldn't go into the statistics
        END IF;
    END IF; -- end test pinning

    --populate radio_signal_location for location and signal interpolation
    IF (TG_OP = 'UPDATE' AND OLD.STATUS = 'STARTED' AND NEW.STATUS = 'FINISHED') -- for ordinary tests
       OR
       ((TG_OP = 'INSERT' OR TG_OP = 'UPDATE') AND NEW.STATUS = 'SIGNAL')        -- for signal measurements
    then
       INSERT INTO radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, "time") select (interpolate_radio_signal_location_v2 (new.open_test_uuid)).*
       ON CONFLICT DO NOTHING; -- conflicting rows will be ignored, the remaining will be inserted
    END IF; --location and signal interpolation

    --debugging, should be commented out for production
    --IF (TG_OP = 'INSERT') THEN RAISE warning 'rmbtdebug:TG_OP=% NEW=%',TG_OP, NEW; END IF;
    --IF (TG_OP = 'UPDATE') THEN RAISE warning 'rmbtdebug:TG_OP=% OLD=%',TG_OP, OLD; RAISE warning 'rmbtdebug:TG_OP=% NEW=%',TG_OP, NEW; END IF;
    --debugging end

    RETURN NEW;

END;
$$;


ALTER FUNCTION public.trigger_test() OWNER TO rmbt;

--
-- Name: trigger_test_location(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_test_location() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE

    _min_accuracy CONSTANT integer := 2000;


BEGIN

    -- post process if location is updated
    IF (TG_OP = 'INSERT' OR NEW.location IS DISTINCT FROM OLD.location) then

        new.geom3857=st_setsrid(new.location,3857);   
        new.geom4326=st_transform(new.geom3857,4326);
       
        -- ignore if location is not accurate
        IF (NEW.location IS NULL OR NEW.geo_accuracy > _min_accuracy) THEN

        ELSE

            -- add dsr id (Austrian dauersiedlungsraum)
            SELECT dsr.id::INTEGER INTO NEW.settlement_type
            FROM dsr
            WHERE within(st_transform(NEW.geom3857, 31287), dsr.geom)
            LIMIT 1;

            -- add Austrian streets and railway (FRC 1,2,3,4,20,21)
            select q1.link_id,
                   linknet_names.link_name,
                   round(ST_DistanceSphere(q1.geom,
                                           ST_Transform(NEW.geom3857,
                                                        4326))) link_distance,
                   q1.frc,
                   q1.edge_id
            into NEW.link_id, NEW.link_name, NEW.link_distance, NEW.frc, NEW.edge_id
            from (SELECT linknet.link_id,
                         linknet.geom,
                         linknet.frc,
                         linknet.edge_id
                  FROM linknet
            -- optimize search by using boundary box on geometry
            -- bbox=ST_Expand(geom,0.01);
            WHERE ST_Transform(NEW.geom3857, 4326) && linknet.bbox

                  ORDER BY ST_Distance(linknet.geom,
                                       ST_Transform(NEW.geom3857,
                                                    4326)) ASC
                  LIMIT 1) as q1
                     LEFT JOIN linknet_names ON q1.link_id = linknet_names.link_id
             WHERE ST_DistanceSphere(q1.geom,
                                   ST_Transform(NEW.geom3857, 4326)) <=
                  10.0
            -- only if accuracy 10m or better
             AND NEW.geo_accuracy < 10.0
            -- only if GPS available
            AND (NEW.geo_provider ='' OR -- iOS (up to now)
                NEW.geo_provider IS NULL OR  -- iOS (planned)
                 NEW.geo_provider='gps');

            -- add BEV gkz (community identifier) and kg_nr (settlement identifier)
            BEGIN
                SELECT bev.gkz::INTEGER,
                       bev.kg_nr_int
                       INTO NEW.gkz_bev, NEW.kg_nr_bev
                FROM bev_vgd bev
                WHERE st_transform(NEW.geom3857, 31287) && bev.bbox
                AND within(st_transform(NEW.geom3857, 31287), bev.geom)
                LIMIT 1;
            EXCEPTION
                WHEN undefined_table THEN
                    -- just return NULL, but ignore missing database
                    RAISE NOTICE '%', SQLERRM;
            END;

            -- add SA gkz (community identifier)
            BEGIN
                SELECT sa.id::INTEGER INTO NEW.gkz_sa
                FROM statistik_austria_gem sa
                WHERE st_transform(NEW.geom3857, 31287) && sa.bbox
                AND
                within(st_transform(NEW.geom3857, 31287), sa.geom)
                LIMIT 1;
            EXCEPTION
                WHEN undefined_table THEN
                    -- just return NULL, but ignore missing database
                    RAISE NOTICE '%', SQLERRM;
            END;

            -- add land cover id
            SELECT clc.code_12::INTEGER INTO NEW.land_cover
            FROM clc12_all_oesterreich clc
            -- use boundary box to increase performance
            -- bbox=ST_EXPAND(geom,0.01)
            WHERE st_transform(NEW.geom3857, 3035) && clc.bbox
              AND within(st_transform(NEW.geom3857, 3035), clc.geom)
            LIMIT 1;

            -- add country code (country_location)
            IF (NEW.gkz_bev IS NOT NULL) THEN -- #659(mod): Austrian communities are more accurate/up-to-date for AT than admin_0_countries
                NEW.country_location = 'AT';
            ELSE
                BEGIN
                  new.country_location=rmbt_get_country_iso_a2(NEW.geom4326);
                  if (new.country_location='AT') then
                     new.country_location=null; -- #659: because admin_0_countries is inaccurate, do not allow to return 'AT'
                  end if;   
                end;        
            END IF;

            -- add altitude level from digital terrain model (DTM) #1203
            SELECT INTO NEW.dtm_level ST_Value(rast, (ST_Transform(ST_SetSRID(ST_MakePoint(NEW.geo_long, NEW.geo_lat), 4326), 31287)))
            FROM dhm
            WHERE st_intersects(rast, (ST_Transform(ST_SetSRID(ST_MakePoint(NEW.geo_long, NEW.geo_lat), 4326), 31287)));


           -- add atraster100 (Austrian 100m grid)
           BEGIN 
              SELECT atraster100.id::VARCHAR INTO NEW.atraster100
              FROM atraster100
              WHERE st_within(NEW.geom3857, atraster100.geom)
              LIMIT 1;
            EXCEPTION
                WHEN undefined_table THEN
                    -- just return NULL, but ignore missing database
                    RAISE NOTICE '%', SQLERRM;                   
            END; 
           
           -- add atraster250 (Austrian 250m grid)
           BEGIN 
              SELECT atraster250.id::VARCHAR INTO NEW.atraster250
              FROM atraster250
              WHERE st_within(NEW.geom3857, atraster250.geom)
              LIMIT 1;
            EXCEPTION
                WHEN undefined_table THEN
                    -- just return NULL, but ignore missing database
                    RAISE NOTICE '%', SQLERRM;                   
            END;
           
           
           
           
           
           
        END IF;

    END IF;
    -- end of location post processing

    RETURN NEW;


END;
$$;


ALTER FUNCTION public.trigger_test_location() OWNER TO postgres;

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: _SCHEMA_VERSION; Type: TABLE; Schema: public; Owner: rmbt_control
--

CREATE TABLE public."_SCHEMA_VERSION" (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public."_SCHEMA_VERSION" OWNER TO rmbt_control;

--
-- Name: admin_0_countries; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.admin_0_countries (
    gid integer NOT NULL,
    featurecla character varying(15),
    scalerank smallint,
    labelrank smallint,
    sovereignt character varying(32),
    sov_a3 character varying(3),
    adm0_dif smallint,
    level smallint,
    type character varying(17),
    tlc character varying(1),
    admin character varying(36),
    adm0_a3 character varying(3),
    geou_dif smallint,
    geounit character varying(36),
    gu_a3 character varying(3),
    su_dif smallint,
    subunit character varying(36),
    su_a3 character varying(3),
    brk_diff smallint,
    name character varying(29),
    name_long character varying(36),
    brk_a3 character varying(3),
    brk_name character varying(32),
    brk_group character varying(17),
    abbrev character varying(16),
    postal character varying(4),
    formal_en character varying(52),
    formal_fr character varying(35),
    name_ciawf character varying(45),
    note_adm0 character varying(16),
    note_brk character varying(63),
    name_sort character varying(36),
    name_alt character varying(19),
    mapcolor7 smallint,
    mapcolor8 smallint,
    mapcolor9 smallint,
    mapcolor13 smallint,
    pop_est double precision,
    pop_rank smallint,
    pop_year smallint,
    gdp_md integer,
    gdp_year smallint,
    economy character varying(26),
    income_grp character varying(23),
    fips_10 character varying(3),
    iso_a2 character varying(5),
    iso_a2_eh character varying(3),
    iso_a3 character varying(3),
    iso_a3_eh character varying(3),
    iso_n3 character varying(3),
    iso_n3_eh character varying(3),
    un_a3 character varying(4),
    wb_a2 character varying(3),
    wb_a3 character varying(3),
    woe_id integer,
    woe_id_eh integer,
    woe_note character varying(167),
    adm0_iso character varying(3),
    adm0_diff character varying(1),
    adm0_tlc character varying(3),
    adm0_a3_us character varying(3),
    adm0_a3_fr character varying(3),
    adm0_a3_ru character varying(3),
    adm0_a3_es character varying(3),
    adm0_a3_cn character varying(3),
    adm0_a3_tw character varying(3),
    adm0_a3_in character varying(3),
    adm0_a3_np character varying(3),
    adm0_a3_pk character varying(3),
    adm0_a3_de character varying(3),
    adm0_a3_gb character varying(3),
    adm0_a3_br character varying(3),
    adm0_a3_il character varying(3),
    adm0_a3_ps character varying(3),
    adm0_a3_sa character varying(3),
    adm0_a3_eg character varying(3),
    adm0_a3_ma character varying(3),
    adm0_a3_pt character varying(3),
    adm0_a3_ar character varying(3),
    adm0_a3_jp character varying(3),
    adm0_a3_ko character varying(3),
    adm0_a3_vn character varying(3),
    adm0_a3_tr character varying(3),
    adm0_a3_id character varying(3),
    adm0_a3_pl character varying(3),
    adm0_a3_gr character varying(3),
    adm0_a3_it character varying(3),
    adm0_a3_nl character varying(3),
    adm0_a3_se character varying(3),
    adm0_a3_bd character varying(3),
    adm0_a3_ua character varying(3),
    adm0_a3_un smallint,
    adm0_a3_wb smallint,
    continent character varying(23),
    region_un character varying(10),
    subregion character varying(25),
    region_wb character varying(26),
    name_len smallint,
    long_len smallint,
    abbrev_len smallint,
    tiny smallint,
    homepart smallint,
    min_zoom double precision,
    min_label double precision,
    max_label double precision,
    label_x double precision,
    label_y double precision,
    ne_id double precision,
    wikidataid character varying(8),
    name_ar character varying(72),
    name_bn character varying(148),
    name_de character varying(46),
    name_en character varying(44),
    name_es character varying(44),
    name_fa character varying(66),
    name_fr character varying(54),
    name_el character varying(86),
    name_he character varying(78),
    name_hi character varying(126),
    name_hu character varying(52),
    name_id character varying(46),
    name_it character varying(48),
    name_ja character varying(63),
    name_ko character varying(47),
    name_nl character varying(49),
    name_pl character varying(47),
    name_pt character varying(43),
    name_ru character varying(86),
    name_sv character varying(57),
    name_tr character varying(42),
    name_uk character varying(91),
    name_ur character varying(67),
    name_vi character varying(56),
    name_zh character varying(33),
    name_zht character varying(33),
    fclass_iso character varying(24),
    tlc_diff character varying(1),
    fclass_tlc character varying(21),
    fclass_us character varying(30),
    fclass_fr character varying(18),
    fclass_ru character varying(14),
    fclass_es character varying(18),
    fclass_cn character varying(24),
    fclass_tw character varying(15),
    fclass_in character varying(14),
    fclass_np character varying(24),
    fclass_pk character varying(15),
    fclass_de character varying(18),
    fclass_gb character varying(18),
    fclass_br character varying(12),
    fclass_il character varying(15),
    fclass_ps character varying(15),
    fclass_sa character varying(15),
    fclass_eg character varying(24),
    fclass_ma character varying(24),
    fclass_pt character varying(18),
    fclass_ar character varying(12),
    fclass_jp character varying(18),
    fclass_ko character varying(18),
    fclass_vn character varying(12),
    fclass_tr character varying(18),
    fclass_id character varying(24),
    fclass_pl character varying(18),
    fclass_gr character varying(18),
    fclass_it character varying(18),
    fclass_nl character varying(18),
    fclass_se character varying(18),
    fclass_bd character varying(24),
    fclass_ua character varying(18),
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE public.admin_0_countries OWNER TO rmbt;

--
-- Name: admin_0_countries_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.admin_0_countries_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.admin_0_countries_gid_seq OWNER TO rmbt;

--
-- Name: admin_0_countries_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.admin_0_countries_gid_seq OWNED BY public.admin_0_countries.gid;


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
-- Name: atraster; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.atraster (
    gid integer NOT NULL,
    id character varying(254),
    name character varying(254),
    geom public.geometry(MultiPolygon,3035)
);


ALTER TABLE public.atraster OWNER TO rmbt;

--
-- Name: atraster100; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.atraster100 (
    gid integer NOT NULL,
    id character varying(254),
    name character varying(254),
    geom public.geometry(MultiPolygon,3857)
);


ALTER TABLE public.atraster100 OWNER TO rmbt;

--
-- Name: atraster100_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.atraster100_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.atraster100_gid_seq OWNER TO rmbt;

--
-- Name: atraster100_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.atraster100_gid_seq OWNED BY public.atraster100.gid;


--
-- Name: atraster250; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.atraster250 (
    gid integer NOT NULL,
    id character varying(254),
    name character varying(254),
    geom public.geometry(MultiPolygon,3857)
);


ALTER TABLE public.atraster250 OWNER TO rmbt;

--
-- Name: atraster250_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.atraster250_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.atraster250_gid_seq OWNER TO rmbt;

--
-- Name: atraster250_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.atraster250_gid_seq OWNED BY public.atraster250.gid;


--
-- Name: atraster_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.atraster_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.atraster_gid_seq OWNER TO rmbt;

--
-- Name: atraster_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.atraster_gid_seq OWNED BY public.atraster.gid;


--
-- Name: bev_vgd; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.bev_vgd (
    gid integer NOT NULL,
    meridian smallint,
    gkz integer,
    bkz smallint,
    fa_nr smallint,
    bl_kz smallint,
    st_kz smallint,
    fl double precision,
    kg_nr character varying(6),
    kg character varying(50),
    pg character varying(50),
    pb character varying(50),
    fa character varying(50),
    gb_kz character varying(3),
    gb character varying(50),
    va_nr character varying(2),
    va character varying(50),
    bl character varying(50),
    st character varying(50),
    geom public.geometry(MultiPolygon,31287),
    kg_nr_int integer,
    bbox public.geometry
);


ALTER TABLE public.bev_vgd OWNER TO rmbt;

--
-- Name: bev_vgd_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.bev_vgd_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bev_vgd_gid_seq OWNER TO rmbt;

--
-- Name: bev_vgd_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.bev_vgd_gid_seq OWNED BY public.bev_vgd.gid;


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
    geom public.geometry(MultiPolygonZM,3035),
    bbox public.geometry
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
-- Name: cov_bb_fixed; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.cov_bb_fixed (
    uid integer NOT NULL,
    raster character varying(50),
    operator character varying(200),
    technology character varying(50),
    dl_max_mbit real,
    ul_max_mbit real,
    date character varying(50)
);


ALTER TABLE public.cov_bb_fixed OWNER TO rmbt;

--
-- Name: cov_bb_fixed_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.cov_bb_fixed_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cov_bb_fixed_uid_seq OWNER TO rmbt;

--
-- Name: cov_bb_fixed_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.cov_bb_fixed_uid_seq OWNED BY public.cov_bb_fixed.uid;


--
-- Name: cov_mno; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.cov_mno (
    uid integer NOT NULL,
    operator character varying(50),
    reference character varying(50),
    license character varying(50),
    rfc_date character varying(50),
    raster character varying(50),
    dl_normal bigint,
    ul_normal bigint,
    dl_max bigint,
    ul_max bigint
);


ALTER TABLE public.cov_mno OWNER TO rmbt;

--
-- Name: cov_mno_fn; Type: MATERIALIZED VIEW; Schema: public; Owner: rmbt
--

CREATE MATERIALIZED VIEW public.cov_mno_fn AS
 SELECT cov_bb_fixed.operator,
    'BBfixed'::character varying AS reference,
    'CCBY4.0 BMLRT'::text AS license,
    substr((cov_bb_fixed.date)::text, 0, 11) AS rfc_date,
    cov_bb_fixed.raster,
    NULL::bigint AS dl_normal,
    NULL::bigint AS ul_normal,
    ((cov_bb_fixed.dl_max_mbit * (1000000)::double precision))::bigint AS dl_max,
    ((cov_bb_fixed.ul_max_mbit * (1000000)::double precision))::bigint AS ul_max,
    cov_bb_fixed.technology
   FROM public.cov_bb_fixed
  WITH NO DATA;


ALTER TABLE public.cov_mno_fn OWNER TO rmbt;

--
-- Name: cov_mno_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.cov_mno_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cov_mno_uid_seq OWNER TO rmbt;

--
-- Name: cov_mno_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.cov_mno_uid_seq OWNED BY public.cov_mno.uid;


--
-- Name: cov_visible_name; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.cov_visible_name (
    uid integer NOT NULL,
    operator character varying(200),
    visible_name character varying(50)
);


ALTER TABLE public.cov_visible_name OWNER TO rmbt;

--
-- Name: cov_visible_name_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.cov_visible_name_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cov_visible_name_uid_seq OWNER TO rmbt;

--
-- Name: cov_visible_name_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.cov_visible_name_uid_seq OWNED BY public.cov_visible_name.uid;


--
-- Name: dhm; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.dhm (
    rid integer NOT NULL,
    rast public.raster
);


ALTER TABLE public.dhm OWNER TO rmbt;

--
-- Name: dhm2_rid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.dhm2_rid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dhm2_rid_seq OWNER TO rmbt;

--
-- Name: dhm2_rid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.dhm2_rid_seq OWNED BY public.dhm.rid;


--
-- Name: dsr; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.dsr (
    gid integer NOT NULL,
    id numeric,
    name character varying(40),
    geom public.geometry(MultiPolygon,31287)
);


ALTER TABLE public.dsr OWNER TO rmbt;

--
-- Name: dsr_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.dsr_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.dsr_gid_seq OWNER TO rmbt;

--
-- Name: dsr_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.dsr_gid_seq OWNED BY public.dsr.gid;


--
-- Name: fix_location; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fix_location (
    uid bigint NOT NULL,
    open_test_uuid uuid NOT NULL,
    geo_location_uuid uuid
);


ALTER TABLE public.fix_location OWNER TO postgres;

--
-- Name: fix_location0; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fix_location0 (
    uid bigint NOT NULL,
    open_test_uuid uuid,
    duration double precision,
    count_ns0 integer
);


ALTER TABLE public.fix_location0 OWNER TO postgres;

--
-- Name: fix_location0_uid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fix_location0_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.fix_location0_uid_seq OWNER TO postgres;

--
-- Name: fix_location0_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fix_location0_uid_seq OWNED BY public.fix_location0.uid;


--
-- Name: fix_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.fix_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.fix_location_uid_seq OWNER TO postgres;

--
-- Name: fix_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.fix_location_uid_seq OWNED BY public.fix_location.uid;


--
-- Name: geo_location; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.geo_location (
    uid bigint NOT NULL,
    geo_location_uuid uuid DEFAULT public.uuid_generate_v4(),
    open_test_uuid uuid NOT NULL,
    test_id bigint,
    time_ns bigint NOT NULL,
    "time" timestamp with time zone,
    accuracy double precision,
    altitude double precision,
    bearing double precision,
    speed double precision,
    provider character varying(20),
    geo_lat double precision,
    geo_long double precision,
    location public.geometry NOT NULL,
    mock_location boolean,
    geom4326 public.geometry(Point,4326),
    geom3857 public.geometry(Point,3857),
    CONSTRAINT enforce_geotype_location CHECK ((public.geometrytype(location) = 'POINT'::text)),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913)),
    CONSTRAINT geo_location_location_check CHECK ((public.st_ndims(location) = 2))
);


ALTER TABLE public.geo_location OWNER TO rmbt;

--
-- Name: geo_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.geo_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.geo_location_uid_seq OWNER TO rmbt;

--
-- Name: geo_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.geo_location_uid_seq OWNED BY public.geo_location.uid;


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
-- Name: link4net; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.link4net (
    gid integer NOT NULL,
    fid numeric,
    link_id numeric,
    name1 character varying(254),
    name2 character varying(254),
    from_node numeric,
    to_node numeric,
    speedcar_t bigint,
    speedcar_b bigint,
    speedtru_t bigint,
    speedtru_b bigint,
    vmax_car_t bigint,
    vmax_car_b bigint,
    vmax_tru_t bigint,
    vmax_tru_b bigint,
    access_tow bigint,
    access_bkw bigint,
    length numeric,
    frc bigint,
    cap_tow numeric,
    cap_bkw numeric,
    lanes_tow numeric,
    lanes_bkw numeric,
    formofway bigint,
    brunnel bigint,
    maxheight numeric,
    maxwidth numeric,
    maxpress numeric,
    abuttercar bigint,
    abuttertru bigint,
    urban bigint,
    width numeric,
    int_level numeric,
    toll bigint,
    baustatus bigint,
    subnet_id bigint,
    oneway_car bigint,
    oneway_bk bigint,
    oneway_bus bigint,
    edge_id numeric,
    edgecat character varying(3),
    regcode character varying(31),
    sustainer character varying(19),
    dbcon bigint,
    geom public.geometry(MultiLineString,4326)
);


ALTER TABLE public.link4net OWNER TO rmbt;

--
-- Name: link4net_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.link4net_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.link4net_gid_seq OWNER TO rmbt;

--
-- Name: link4net_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.link4net_gid_seq OWNED BY public.link4net.gid;


--
-- Name: linknet; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.linknet (
    gid integer NOT NULL,
    link_id bigint,
    name1 character varying(254),
    name2 character varying(254),
    from_node bigint,
    to_node bigint,
    speedcar_t smallint,
    speedcar_b smallint,
    speedtru_t smallint,
    speedtru_b smallint,
    vmax_car_t smallint,
    vmax_car_b smallint,
    vmax_tru_t smallint,
    vmax_tru_b smallint,
    access_tow integer,
    access_bkw integer,
    length double precision,
    frc smallint,
    cap_tow bigint,
    cap_bkw bigint,
    lanes_tow double precision,
    lanes_bkw double precision,
    formofway smallint,
    brunnel smallint,
    maxheight double precision,
    maxwidth double precision,
    maxpress double precision,
    abuttercar smallint,
    abuttertru smallint,
    urban integer,
    width double precision,
    int_level double precision,
    toll smallint,
    baustatus smallint,
    subnet_id integer,
    oneway_car smallint,
    oneway_bk smallint,
    oneway_bus smallint,
    edge_id numeric,
    edgecat character varying(3),
    regcode character varying(31),
    sustainer character varying(19),
    dbcon smallint,
    geom public.geometry(MultiLineString,4326),
    bbox public.geometry
);


ALTER TABLE public.linknet OWNER TO rmbt;

--
-- Name: linknet_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.linknet_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linknet_gid_seq OWNER TO rmbt;

--
-- Name: linknet_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.linknet_gid_seq OWNED BY public.linknet.gid;


--
-- Name: linknet_names; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.linknet_names (
    link_id integer NOT NULL,
    link_name character varying
);


ALTER TABLE public.linknet_names OWNER TO rmbt;

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
    geom public.geometry(MultiPolygon,900913),
    geom4326 public.geometry(MultiPolygon,4326)
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
    uuid uuid,
    start_time timestamp with time zone DEFAULT now() NOT NULL,
    end_time timestamp with time zone
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
-- Name: news_view; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.news_view AS
 SELECT n.uid,
    n.title_en,
    n.title_de,
    n.text_en,
    n.text_de,
    n.plattform,
    n.active,
    n.force,
    n.max_software_version_code,
    n.min_software_version_code,
    n.uuid,
    n.start_time,
    n.end_time,
    n."time",
    public.getnewsstatus(n.active, n.start_time, n.end_time) AS status
   FROM public.news n;


ALTER TABLE public.news_view OWNER TO postgres;

--
-- Name: next_link_uid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.next_link_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.next_link_uid_seq OWNER TO postgres;

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
-- Name: qoe_classification; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qoe_classification (
    uid integer NOT NULL,
    category character varying(30) NOT NULL,
    dl_4 double precision NOT NULL,
    dl_3 double precision NOT NULL,
    dl_2 double precision NOT NULL,
    ul_4 double precision NOT NULL,
    ul_3 double precision NOT NULL,
    ul_2 double precision NOT NULL,
    ping_4 double precision NOT NULL,
    ping_3 double precision NOT NULL,
    ping_2 double precision NOT NULL
);


ALTER TABLE public.qoe_classification OWNER TO rmbt;

--
-- Name: qoe_classification_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qoe_classification_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qoe_classification_uid_seq OWNER TO rmbt;

--
-- Name: qoe_classification_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qoe_classification_uid_seq OWNED BY public.qoe_classification.uid;


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
-- Name: qos_test_result_b; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.qos_test_result_b (
    uid integer NOT NULL,
    test_uid bigint,
    qos_test_uid bigint,
    success_count integer DEFAULT 0 NOT NULL,
    failure_count integer DEFAULT 0 NOT NULL,
    implausible boolean DEFAULT false,
    deleted boolean DEFAULT false,
    result jsonb
);


ALTER TABLE public.qos_test_result_b OWNER TO rmbt;

--
-- Name: qos_test_result_b_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.qos_test_result_b_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.qos_test_result_b_uid_seq OWNER TO rmbt;

--
-- Name: qos_test_result_b_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.qos_test_result_b_uid_seq OWNED BY public.qos_test_result_b.uid;


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
    location_id bigint,
    area_code integer,
    primary_scrambling_code integer,
    registered boolean,
    channel_number integer,
    active boolean,
    primary_data_subscription character varying(30),
    cell_state character varying(15)
);


ALTER TABLE public.radio_cell OWNER TO rmbt;

--
-- Name: COLUMN radio_cell.registered; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.radio_cell.registered IS 'do not use, obsolete';


--
-- Name: COLUMN radio_cell.cell_state; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.radio_cell.cell_state IS 'Connection status of cell: "primary","secondary","none"';


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
    radio_signal_uuid uuid DEFAULT public.uuid_generate_v4(),
    open_test_uuid uuid NOT NULL,
    cell_uuid uuid NOT NULL,
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
-- Name: radio_signal_location; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.radio_signal_location (
    uid bigint NOT NULL,
    last_signal_uuid uuid,
    last_radio_signal_uuid uuid,
    last_geo_location_uuid uuid NOT NULL,
    open_test_uuid uuid NOT NULL,
    interpolated_location public.geometry NOT NULL,
    "time" timestamp with time zone NOT NULL,
    next_geo_location_uuid uuid,
    CONSTRAINT location_not_null_for_uuid CHECK (((last_geo_location_uuid IS NOT NULL) AND (interpolated_location IS NOT NULL))),
    CONSTRAINT xor_signals_not_null CHECK ((((last_signal_uuid IS NOT NULL) AND (last_radio_signal_uuid IS NULL)) OR ((last_signal_uuid IS NULL) AND (last_radio_signal_uuid IS NOT NULL))))
);


ALTER TABLE public.radio_signal_location OWNER TO rmbt;

--
-- Name: COLUMN radio_signal_location."time"; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.radio_signal_location."time" IS 'equals test.time + time_ns';


--
-- Name: radio_signal_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.radio_signal_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.radio_signal_location_uid_seq OWNER TO rmbt;

--
-- Name: radio_signal_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.radio_signal_location_uid_seq OWNED BY public.radio_signal_location.uid;


--
-- Name: radio_signal_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.radio_signal_uid_seq
    AS integer
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
    signal_uuid uuid DEFAULT public.uuid_generate_v4(),
    test_id bigint,
    open_test_uuid uuid NOT NULL,
    "time" timestamp with time zone,
    time_ns bigint,
    signal_strength integer,
    network_type_id integer,
    wifi_link_speed integer,
    gsm_bit_error_rate integer,
    wifi_rssi integer,
    lte_rsrp integer,
    lte_rsrq integer,
    lte_rssnr integer,
    lte_cqi integer
);


ALTER TABLE public.signal OWNER TO rmbt;

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
-- Name: statistik_austria_gem; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.statistik_austria_gem (
    gid integer NOT NULL,
    id character varying(254),
    name character varying(254),
    geom public.geometry(MultiPolygon,31287),
    bbox public.geometry
);


ALTER TABLE public.statistik_austria_gem OWNER TO rmbt;

--
-- Name: statistik_austria_gem_gid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.statistik_austria_gem_gid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.statistik_austria_gem_gid_seq OWNER TO rmbt;

--
-- Name: statistik_austria_gem_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.statistik_austria_gem_gid_seq OWNED BY public.statistik_austria_gem.gid;


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
    gkz_obsolete integer,
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
    gkz_bev_obsolete integer,
    gkz_sa_obsolete integer,
    kg_nr_bev integer,
    land_cover_obsolete integer,
    cell_area_code integer,
    cell_location_id integer,
    link_distance_obsolete integer,
    link_id_obsolete integer,
    settlement_type_obsolete integer,
    link_name_obsolete character varying,
    frc_obsolete smallint,
    edge_id_obsolete numeric,
    geo_location_uuid uuid,
    last_client_status character varying(50),
    last_qos_status character varying(50),
    test_error_cause character varying,
    last_sequence_number integer,
    submission_retry_count integer,
    measurement_type_flag character varying(50),
    geom3857 public.geometry(Point,3857),
    geom4326 public.geometry(Point,4326),
    temperature double precision,
    coverage boolean,
    referrer character varying(2048),
    CONSTRAINT enforce_dims_location CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((public.geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913)),
    CONSTRAINT test_speed_download_noneg CHECK ((speed_download >= 0)),
    CONSTRAINT test_speed_upload_noneg CHECK ((speed_upload >= 0))
);


ALTER TABLE public.test OWNER TO rmbt;

--
-- Name: COLUMN test.server_id; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test.server_id IS 'id of test server used';


--
-- Name: COLUMN test.coverage; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test.coverage IS 'True if measurement is a coverage verification test';


--
-- Name: COLUMN test.referrer; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test.referrer IS 'referrer for iframe tests, eg. "https://www.rtr.at/abcde/x.hml"';


--
-- Name: test_location; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_location (
    uid bigint NOT NULL,
    open_test_uuid uuid NOT NULL,
    geo_location_uuid uuid NOT NULL,
    location public.geometry NOT NULL,
    geo_long double precision,
    geo_lat double precision,
    geo_accuracy double precision,
    geo_provider character varying,
    kg_nr_bev integer,
    gkz_bev integer,
    gkz_sa integer,
    land_cover integer,
    settlement_type integer,
    link_id integer,
    link_name character varying,
    link_distance integer,
    frc smallint,
    edge_id numeric,
    country_location character(2),
    dtm_level integer,
    geom3857 public.geometry(Point,3857),
    geom4326 public.geometry(Point,4326),
    atraster100 character varying(16),
    atraster250 character varying(18),
    CONSTRAINT enforce_dims_location2 CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location2 CHECK ((public.geometrytype(location) = 'POINT'::text)),
    CONSTRAINT settlement_type_check2 CHECK (((settlement_type > 0) AND (settlement_type < 4)))
);


ALTER TABLE public.test_location OWNER TO rmbt;

--
-- Name: COLUMN test_location.atraster100; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test_location.atraster100 IS 'Austrian 100m grid ID';


--
-- Name: COLUMN test_location.atraster250; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test_location.atraster250 IS 'Austrian 250m grid ID';


--
-- Name: test_location_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_location_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_location_uid_seq OWNER TO rmbt;

--
-- Name: test_location_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_location_uid_seq OWNED BY public.test_location.uid;


--
-- Name: test_loopmode; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_loopmode (
    uid integer NOT NULL,
    test_uuid uuid,
    client_uuid uuid,
    max_movement integer,
    max_delay integer,
    max_tests integer,
    test_counter integer,
    loop_uuid uuid
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
    time_end_ns bigint,
    open_test_uuid uuid
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
    archived boolean DEFAULT false NOT NULL,
    coverage boolean,
    CONSTRAINT enforce_dims_location CHECK ((public.st_ndims(location) = 2)),
    CONSTRAINT enforce_geotype_location CHECK (((public.geometrytype(location) = 'POINT'::text) OR (location IS NULL))),
    CONSTRAINT enforce_srid_location CHECK ((public.st_srid(location) = 900913))
);


ALTER TABLE public.test_server OWNER TO rmbt;

--
-- Name: COLUMN test_server.coverage; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON COLUMN public.test_server.coverage IS 'True if server is for coverage verification tests';


--
-- Name: test_server_types; Type: TABLE; Schema: public; Owner: rmbt
--

CREATE TABLE public.test_server_types (
    test_server_uid bigint NOT NULL,
    server_type character varying(60) NOT NULL,
    uid integer NOT NULL,
    port integer,
    port_ssl integer,
    encrypted boolean DEFAULT false NOT NULL
);


ALTER TABLE public.test_server_types OWNER TO rmbt;

--
-- Name: test_server_types_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.test_server_types_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.test_server_types_uid_seq OWNER TO rmbt;

--
-- Name: test_server_types_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: rmbt
--

ALTER SEQUENCE public.test_server_types_uid_seq OWNED BY public.test_server_types.uid;


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
-- Name: tl2_uid_seq; Type: SEQUENCE; Schema: public; Owner: rmbt
--

CREATE SEQUENCE public.tl2_uid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tl2_uid_seq OWNER TO rmbt;

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
   FROM public.get_replication_delay() get_replication_delay(pid, usesysid, usename, application_name, client_addr, client_hostname, client_port, backend_start, backend_xmin, state, sent_lsn, write_lsn, flush_lsn, replay_lsn, write_lag, flush_lag, replay_lag, sync_priority, sync_state, reply_time);


ALTER TABLE public.v_get_replication_delay OWNER TO postgres;

--
-- Name: v_kibana; Type: VIEW; Schema: public; Owner: rmbt
--

CREATE VIEW public.v_kibana AS
 SELECT test."time" AS "timestamp",
    test."time" AS measurement_date,
    test.open_test_uuid,
    provider.shortname AS provider,
    test.plattform AS platform,
    test.device,
    test.model,
    test.speed_download AS download,
    test.speed_upload AS upload,
    test.ping_median AS ping,
    (test.network_type)::text AS network_type,
    test.num_threads,
    test.signal_strength,
    test.lte_rsrq,
    test.lte_rsrp,
    NULL::character varying AS client_uuid,
    NULL::character varying AS loop_mode_uuid,
    test.country_asn AS country,
    test.client_name,
    test.client_version,
    NULL::character varying AS ip_address,
    test.status,
    NULL::character varying AS wifi_ssid,
    test.network_operator_name AS operator,
        CASE
            WHEN (test.geo_accuracy < (10000)::double precision) THEN test.geo_lat
            ELSE NULL::double precision
        END AS latitude,
        CASE
            WHEN (test.geo_accuracy < (10000)::double precision) THEN test.geo_long
            ELSE NULL::double precision
        END AS longitude,
    test_server.uid AS measurement_server_id,
    test_server.name AS measurement_server_name,
    NULL::character varying AS client_type,
    test.os_version,
    test.api_level,
    test.product,
    test.phone_type,
    test.data_state,
    test.network_country,
    test.network_operator,
    test.network_operator_name,
    test.network_sim_country,
    test.network_sim_operator,
    test.network_sim_operator_name,
    test.duration,
    test.timezone,
    test.bytes_download,
    test.bytes_upload,
    test.nsec_download,
    test.nsec_upload,
    test.server_ip,
    test.software_revision,
    test.nat_type,
    test.public_ip_asn,
    test.total_bytes_download,
    test.total_bytes_upload,
    test.wifi_link_speed,
    test.public_ip_as_name,
    test.network_is_roaming,
    test.geo_provider,
        CASE
            WHEN (test.geo_accuracy < (10000)::double precision) THEN test.geo_accuracy
            ELSE NULL::double precision
        END AS geo_accuracy,
    test.roaming_type,
    test.country_asn,
    test.test_if_bytes_download,
    test.test_if_bytes_upload,
    test.testdl_if_bytes_download,
    test.testdl_if_bytes_upload,
    test.testul_if_bytes_download,
    test.testul_if_bytes_upload,
    test.country_geoip,
    test.network_group_name,
    test.network_group_type,
    test.time_dl_ns,
    test.time_ul_ns,
    test.num_threads_ul,
    tl.kg_nr_bev,
    tl.gkz_bev,
    tl.gkz_sa,
    tl.land_cover,
    tl.settlement_type,
    tl.country_location,
    tl.link_name,
    tl.dtm_level
   FROM (((public.test
     LEFT JOIN public.test_location tl ON ((tl.open_test_uuid = test.open_test_uuid)))
     LEFT JOIN public.provider ON ((provider.uid = test.provider_id)))
     LEFT JOIN public.test_server ON ((test_server.uid = test.server_id)))
  WHERE (((test.status)::text = 'FINISHED'::text) AND (NOT test.deleted) AND (NOT test.implausible))
  ORDER BY test.uid;


ALTER TABLE public.v_kibana OWNER TO rmbt;

--
-- Name: v_radio_signal_location; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_radio_signal_location AS
 SELECT radio_signal_location.uid,
    radio_signal_location.last_signal_uuid,
    radio_signal_location.last_radio_signal_uuid,
    radio_signal_location.last_geo_location_uuid,
    radio_signal_location.open_test_uuid,
    radio_signal_location.interpolated_location,
    geo_location.accuracy AS geo_accuracy,
    radio_signal_location."time",
    COALESCE((radio_signal.lte_rsrp + 10), radio_signal.signal_strength, (signal.lte_rsrp + 10), signal.signal_strength) AS merged_signal,
    COALESCE(radio_signal.network_type_id, signal.network_type_id) AS network_type,
    test.deleted,
    test.implausible,
    test.status
   FROM ((((public.radio_signal_location
     LEFT JOIN public.radio_signal ON ((radio_signal_location.last_radio_signal_uuid = radio_signal.radio_signal_uuid)))
     LEFT JOIN public.signal ON ((radio_signal_location.last_signal_uuid = signal.signal_uuid)))
     JOIN public.geo_location ON ((radio_signal_location.last_geo_location_uuid = geo_location.geo_location_uuid)))
     JOIN public.test ON ((radio_signal.open_test_uuid = test.open_test_uuid)));


ALTER TABLE public.v_radio_signal_location OWNER TO postgres;

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
    test.gkz_obsolete AS gkz
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
    test.gkz_obsolete AS gkz,
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
    test.gkz_obsolete AS gkz,
    test.kg_nr_bev,
    test.user_server_selection
   FROM public.test;


ALTER TABLE public.v_test3 OWNER TO postgres;

--
-- Name: vt; Type: VIEW; Schema: public; Owner: rmbt
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
    test.gkz_obsolete AS gkz,
    test.android_permissions,
    test.dual_sim_detection_method,
    test.pinned,
    test.similar_test_uid
   FROM public.test;


ALTER TABLE public.vt OWNER TO rmbt;

--
-- Name: VIEW vt; Type: COMMENT; Schema: public; Owner: rmbt
--

COMMENT ON VIEW public.vt IS 'light weight columns from test (dj)';


--
-- Name: admin_0_countries gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.admin_0_countries ALTER COLUMN gid SET DEFAULT nextval('public.admin_0_countries_gid_seq'::regclass);


--
-- Name: as2provider uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.as2provider ALTER COLUMN uid SET DEFAULT nextval('public.as2provider_uid_seq'::regclass);


--
-- Name: atraster gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster ALTER COLUMN gid SET DEFAULT nextval('public.atraster_gid_seq'::regclass);


--
-- Name: atraster100 gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster100 ALTER COLUMN gid SET DEFAULT nextval('public.atraster100_gid_seq'::regclass);


--
-- Name: atraster250 gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster250 ALTER COLUMN gid SET DEFAULT nextval('public.atraster250_gid_seq'::regclass);


--
-- Name: bev_vgd gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.bev_vgd ALTER COLUMN gid SET DEFAULT nextval('public.bev_vgd_gid_seq'::regclass);


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
-- Name: cov_bb_fixed uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_bb_fixed ALTER COLUMN uid SET DEFAULT nextval('public.cov_bb_fixed_uid_seq'::regclass);


--
-- Name: cov_mno uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_mno ALTER COLUMN uid SET DEFAULT nextval('public.cov_mno_uid_seq'::regclass);


--
-- Name: cov_visible_name uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_visible_name ALTER COLUMN uid SET DEFAULT nextval('public.cov_visible_name_uid_seq'::regclass);


--
-- Name: device_map uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map ALTER COLUMN uid SET DEFAULT nextval('public.android_device_map_uid_seq'::regclass);


--
-- Name: dhm rid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.dhm ALTER COLUMN rid SET DEFAULT nextval('public.dhm2_rid_seq'::regclass);


--
-- Name: dsr gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.dsr ALTER COLUMN gid SET DEFAULT nextval('public.dsr_gid_seq'::regclass);


--
-- Name: fix_location uid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fix_location ALTER COLUMN uid SET DEFAULT nextval('public.fix_location_uid_seq'::regclass);


--
-- Name: fix_location0 uid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fix_location0 ALTER COLUMN uid SET DEFAULT nextval('public.fix_location0_uid_seq'::regclass);


--
-- Name: geo_location uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location ALTER COLUMN uid SET DEFAULT nextval('public.geo_location_uid_seq'::regclass);


--
-- Name: link4net gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.link4net ALTER COLUMN gid SET DEFAULT nextval('public.link4net_gid_seq'::regclass);


--
-- Name: linknet gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.linknet ALTER COLUMN gid SET DEFAULT nextval('public.linknet_gid_seq'::regclass);


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
-- Name: ping uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.ping ALTER COLUMN uid SET DEFAULT nextval('public.ping_uid_seq'::regclass);


--
-- Name: provider uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.provider ALTER COLUMN uid SET DEFAULT nextval('public.provider_uid_seq'::regclass);


--
-- Name: qoe_classification uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qoe_classification ALTER COLUMN uid SET DEFAULT nextval('public.qoe_classification_uid_seq'::regclass);


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
-- Name: qos_test_result_b uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result_b ALTER COLUMN uid SET DEFAULT nextval('public.qos_test_result_b_uid_seq'::regclass);


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
-- Name: radio_signal_location uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location ALTER COLUMN uid SET DEFAULT nextval('public.radio_signal_location_uid_seq'::regclass);


--
-- Name: settings uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.settings ALTER COLUMN uid SET DEFAULT nextval('public.settings_uid_seq'::regclass);


--
-- Name: signal uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal ALTER COLUMN uid SET DEFAULT nextval('public.signal_uid_seq'::regclass);


--
-- Name: statistik_austria_gem gid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.statistik_austria_gem ALTER COLUMN gid SET DEFAULT nextval('public.statistik_austria_gem_gid_seq'::regclass);


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
-- Name: test_location uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_location ALTER COLUMN uid SET DEFAULT nextval('public.test_location_uid_seq'::regclass);


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
-- Name: test_server_types uid; Type: DEFAULT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_server_types ALTER COLUMN uid SET DEFAULT nextval('public.test_server_types_uid_seq'::regclass);


--
-- Name: _SCHEMA_VERSION _SCHEMA_VERSION_pk; Type: CONSTRAINT; Schema: public; Owner: rmbt_control
--

ALTER TABLE ONLY public."_SCHEMA_VERSION"
    ADD CONSTRAINT "_SCHEMA_VERSION_pk" PRIMARY KEY (installed_rank);


--
-- Name: admin_0_countries admin_0_countries_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.admin_0_countries
    ADD CONSTRAINT admin_0_countries_pkey PRIMARY KEY (gid);


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
-- Name: atraster100 atraster100_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster100
    ADD CONSTRAINT atraster100_pkey PRIMARY KEY (gid);


--
-- Name: atraster250 atraster250_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster250
    ADD CONSTRAINT atraster250_pkey PRIMARY KEY (gid);


--
-- Name: atraster atraster_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.atraster
    ADD CONSTRAINT atraster_pkey PRIMARY KEY (gid);


--
-- Name: bev_vgd bev_vgd_kg_nr_int; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.bev_vgd
    ADD CONSTRAINT bev_vgd_kg_nr_int UNIQUE (kg_nr_int);


--
-- Name: bev_vgd bev_vgd_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.bev_vgd
    ADD CONSTRAINT bev_vgd_pkey PRIMARY KEY (gid);


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
-- Name: cov_bb_fixed cov_bb_fixed_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_bb_fixed
    ADD CONSTRAINT cov_bb_fixed_pkey PRIMARY KEY (uid);


--
-- Name: cov_mno cov_mno_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_mno
    ADD CONSTRAINT cov_mno_pkey PRIMARY KEY (uid);


--
-- Name: cov_visible_name cov_visible_name_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.cov_visible_name
    ADD CONSTRAINT cov_visible_name_pkey PRIMARY KEY (uid);


--
-- Name: device_map device_map_fullname_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.device_map
    ADD CONSTRAINT device_map_fullname_key UNIQUE (fullname);


--
-- Name: dhm dhm2_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.dhm
    ADD CONSTRAINT dhm2_pkey PRIMARY KEY (rid);


--
-- Name: dsr dsr_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.dsr
    ADD CONSTRAINT dsr_pkey PRIMARY KEY (gid);


--
-- Name: fix_location0 fix_location0_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fix_location0
    ADD CONSTRAINT fix_location0_pkey PRIMARY KEY (uid);


--
-- Name: fix_location fix_location_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fix_location
    ADD CONSTRAINT fix_location_pkey PRIMARY KEY (uid);


--
-- Name: geo_location geo_location_geo_location_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location
    ADD CONSTRAINT geo_location_geo_location_uuid_key UNIQUE (geo_location_uuid);


--
-- Name: json_sender json_sender_sender_id_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.json_sender
    ADD CONSTRAINT json_sender_sender_id_key UNIQUE (sender_id);


--
-- Name: link4net link4net_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.link4net
    ADD CONSTRAINT link4net_pkey PRIMARY KEY (gid);


--
-- Name: linknet_names linknet_names_pk; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.linknet_names
    ADD CONSTRAINT linknet_names_pk PRIMARY KEY (link_id);


--
-- Name: linknet linknet_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.linknet
    ADD CONSTRAINT linknet_pkey PRIMARY KEY (gid);


--
-- Name: geo_location location_uid_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location
    ADD CONSTRAINT location_uid_pkey PRIMARY KEY (uid);


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
-- Name: qoe_classification qoe_classification_pk; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qoe_classification
    ADD CONSTRAINT qoe_classification_pk PRIMARY KEY (uid);


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
-- Name: qos_test_result_b qos_test_resultb_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result_b
    ADD CONSTRAINT qos_test_resultb_pkey PRIMARY KEY (uid);


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
-- Name: radio_signal_location radio_signal_location_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT radio_signal_location_pkey PRIMARY KEY (uid);


--
-- Name: radio_signal radio_signal_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal
    ADD CONSTRAINT radio_signal_pkey PRIMARY KEY (uid);


--
-- Name: radio_signal radio_signal_signal_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal
    ADD CONSTRAINT radio_signal_signal_uuid_key UNIQUE (radio_signal_uuid);


--
-- Name: signal radio_signal_uid_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal
    ADD CONSTRAINT radio_signal_uid_pkey PRIMARY KEY (uid);


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
-- Name: test settlement_type_check; Type: CHECK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE public.test
    ADD CONSTRAINT settlement_type_check CHECK (((settlement_type_obsolete > 0) AND (settlement_type_obsolete < 4))) NOT VALID;


--
-- Name: signal signal_signal_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal
    ADD CONSTRAINT signal_signal_uuid_key UNIQUE (signal_uuid);


--
-- Name: speed speed_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.speed
    ADD CONSTRAINT speed_pkey PRIMARY KEY (open_test_uuid);


--
-- Name: statistik_austria_gem statistik_austria_gem_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.statistik_austria_gem
    ADD CONSTRAINT statistik_austria_gem_pkey PRIMARY KEY (gid);


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
-- Name: test_location test_location_open_test_uuid_key; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_location
    ADD CONSTRAINT test_location_open_test_uuid_key UNIQUE (open_test_uuid);


--
-- Name: test_location test_location_uid_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_location
    ADD CONSTRAINT test_location_uid_pkey PRIMARY KEY (uid);


--
-- Name: test_loopmode test_loopmode_pkey; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode
    ADD CONSTRAINT test_loopmode_pkey PRIMARY KEY (uid);


--
-- Name: test_loopmode test_loopmode_test_uuid_fkey_unique; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_loopmode
    ADD CONSTRAINT test_loopmode_test_uuid_fkey_unique UNIQUE (test_uuid);


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
-- Name: test test_open_test_uuid_unique; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_open_test_uuid_unique UNIQUE (open_test_uuid);


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
-- Name: radio_signal_location unique_radio_signal_and_geo; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT unique_radio_signal_and_geo UNIQUE (last_radio_signal_uuid, last_geo_location_uuid);


--
-- Name: radio_signal_location unique_signal_and_geo; Type: CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT unique_signal_and_geo UNIQUE (last_signal_uuid, last_geo_location_uuid);


--
-- Name: _SCHEMA_VERSION_s_idx; Type: INDEX; Schema: public; Owner: rmbt_control
--

CREATE INDEX "_SCHEMA_VERSION_s_idx" ON public."_SCHEMA_VERSION" USING btree (success);


--
-- Name: admin_0_countries_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX admin_0_countries_geom_idx ON public.admin_0_countries USING gist (geom);


--
-- Name: as2provider_provider_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX as2provider_provider_id_idx ON public.as2provider USING btree (provider_id);


--
-- Name: atraster100_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster100_geom_idx ON public.atraster100 USING gist (geom);


--
-- Name: atraster100_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster100_id_idx ON public.atraster USING btree (id);


--
-- Name: atraster250_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster250_geom_idx ON public.atraster250 USING gist (geom);


--
-- Name: atraster250_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster250_id_idx ON public.atraster USING btree (id);


--
-- Name: atraster_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster_geom_idx ON public.atraster USING gist (geom);


--
-- Name: atraster_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX atraster_id_idx ON public.atraster USING btree (id);


--
-- Name: bev_vgd_bbox_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX bev_vgd_bbox_gix ON public.bev_vgd USING gist (bbox);


--
-- Name: bev_vgd_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX bev_vgd_geom_idx ON public.bev_vgd USING gist (geom);


--
-- Name: bev_vgd_gkz_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX bev_vgd_gkz_idx ON public.bev_vgd USING btree (gkz);


--
-- Name: bev_vgd_kg_nr_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX bev_vgd_kg_nr_idx ON public.bev_vgd USING btree (kg_nr);


--
-- Name: bev_vgd_kg_nr_int_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX bev_vgd_kg_nr_int_gix ON public.bev_vgd USING btree (kg_nr_int);


--
-- Name: cell_location_test_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cell_location_test_id_idx ON public.cell_location USING btree (test_id);


--
-- Name: cell_location_test_id_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cell_location_test_id_time_idx ON public.cell_location USING btree (test_id, "time");


--
-- Name: clc12_all_oesterreich_bbox_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX clc12_all_oesterreich_bbox_gix ON public.clc12_all_oesterreich USING gist (bbox);


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
-- Name: cov_bb_fixed_raster_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cov_bb_fixed_raster_idx ON public.cov_bb_fixed USING btree (raster);


--
-- Name: cov_mno_operator_reference_license_raster_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cov_mno_operator_reference_license_raster_idx ON public.cov_mno USING btree (operator, reference, license, raster);


--
-- Name: cov_mno_raster_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cov_mno_raster_idx ON public.cov_mno USING btree (raster);


--
-- Name: cov_visible_name_visible_name_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX cov_visible_name_visible_name_idx ON public.cov_visible_name USING btree (visible_name);


--
-- Name: dhm2_st_convexhull_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX dhm2_st_convexhull_idx ON public.dhm USING gist (public.st_convexhull(rast));


--
-- Name: download_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX download_idx ON public.test USING btree (bytes_download, network_type);


--
-- Name: dsr_geom_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX dsr_geom_gix ON public.dsr USING gist (geom);


--
-- Name: fki_qos_test_result_qos_test_uid_fkey; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_qos_test_uid_fkey ON public.qos_test_result USING btree (qos_test_uid);


--
-- Name: fki_qos_test_result_qos_testb_uid_fkey; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_qos_testb_uid_fkey ON public.qos_test_result_b USING btree (qos_test_uid);


--
-- Name: fki_qos_test_result_test_uid; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_test_uid ON public.qos_test_result USING btree (test_uid);


--
-- Name: fki_qos_test_result_testb_uid; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX fki_qos_test_result_testb_uid ON public.qos_test_result_b USING btree (test_uid);


--
-- Name: geo_location_open_test_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX geo_location_open_test_uuid_idx ON public.geo_location USING btree (open_test_uuid);


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
-- Name: idx_the_geom_4326_atraster100; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX idx_the_geom_4326_atraster100 ON public.atraster100 USING gist (public.st_transform(geom, 4326));


--
-- Name: idx_the_geom_4326_atraster250; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX idx_the_geom_4326_atraster250 ON public.atraster250 USING gist (public.st_transform(geom, 4326));


--
-- Name: link4net_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX link4net_gix ON public.link4net USING gist (geom);


--
-- Name: link4net_link_id_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX link4net_link_id_idx ON public.link4net USING btree (link_id);


--
-- Name: linknet_bbox_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX linknet_bbox_gix ON public.linknet USING gist (bbox);


--
-- Name: linknet_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX linknet_gix ON public.linknet USING gist (geom);


--
-- Name: linknet_names_link_id_uindex; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE UNIQUE INDEX linknet_names_link_id_uindex ON public.linknet_names USING btree (link_id);


--
-- Name: linknet_names_link_name_index; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX linknet_names_link_name_index ON public.linknet_names USING btree (link_name);


--
-- Name: location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX location_idx ON public.test USING gist (location);


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
-- Name: ne_10m_admin_0_countries_iso_geom3426_gist; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX ne_10m_admin_0_countries_iso_geom3426_gist ON public.ne_10m_admin_0_countries USING gist (geom4326);


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
-- Name: open_test_uuid_cell_location_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_cell_location_idx ON public.cell_location USING btree (open_test_uuid);


--
-- Name: open_test_uuid_ping_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_ping_idx ON public.ping USING btree (open_test_uuid);


--
-- Name: open_test_uuid_signal2_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX open_test_uuid_signal2_idx ON public.signal USING btree (open_test_uuid);


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
-- Name: radio_signal_location_interpolated_location_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_signal_location_interpolated_location_gix ON public.radio_signal_location USING gist (interpolated_location);


--
-- Name: radio_signal_location_open_test_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_signal_location_open_test_uuid_idx ON public.radio_signal_location USING hash (open_test_uuid);


--
-- Name: radio_signal_location_time_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_signal_location_time_idx ON public.radio_signal_location USING btree ("time");


--
-- Name: radio_signal_open_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX radio_signal_open_uuid_idx ON public.radio_signal USING btree (open_test_uuid);


--
-- Name: settings_key_lang_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX settings_key_lang_idx ON public.settings USING btree (key, lang);


--
-- Name: statistik_austria_gem_bbox_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX statistik_austria_gem_bbox_gix ON public.statistik_austria_gem USING gist (bbox);


--
-- Name: statistik_austria_gem_geom_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX statistik_austria_gem_geom_idx ON public.statistik_austria_gem USING gist (geom);


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

CREATE INDEX test_gkz_bev_idx ON public.test USING btree (gkz_bev_obsolete);


--
-- Name: test_gkz_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_gkz_idx ON public.test USING btree (gkz_obsolete);


--
-- Name: test_gkz_sa_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_gkz_sa_idx ON public.test USING btree (gkz_sa_obsolete);


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

CREATE INDEX test_land_cover_idx ON public.test USING btree (land_cover_obsolete);


--
-- Name: test_location_geo_accuracy_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_geo_accuracy_idx ON public.test_location USING btree (geo_accuracy);


--
-- Name: test_location_gkz_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_gkz_bev_idx ON public.test_location USING btree (gkz_bev);


--
-- Name: test_location_gkz_sa_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_gkz_sa_idx ON public.test_location USING btree (gkz_sa);


--
-- Name: test_location_kg_nv_bev_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_kg_nv_bev_idx ON public.test_location USING btree (kg_nr_bev);


--
-- Name: test_location_land_cover_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_land_cover_idx ON public.test_location USING btree (land_cover);


--
-- Name: test_location_link_name_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_link_name_idx ON public.test_location USING btree (link_name);


--
-- Name: test_location_location_gix; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_location_gix ON public.test_location USING gist (location);


--
-- Name: test_location_open_test_uuid_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_open_test_uuid_idx ON public.test_location USING btree (open_test_uuid);


--
-- Name: test_location_settlement_type_idx; Type: INDEX; Schema: public; Owner: rmbt
--

CREATE INDEX test_location_settlement_type_idx ON public.test_location USING btree (settlement_type);


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
-- Name: cell_location trigger_cell_location; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_cell_location BEFORE INSERT ON public.cell_location FOR EACH ROW EXECUTE FUNCTION public.trigger_radio_cell();


--
-- Name: geo_location trigger_geo_location; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_geo_location BEFORE INSERT ON public.geo_location FOR EACH ROW EXECUTE FUNCTION public.trigger_geo_location();


--
-- Name: qos_test_result trigger_qos_test_result; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_qos_test_result BEFORE INSERT OR UPDATE ON public.qos_test_result FOR EACH ROW EXECUTE FUNCTION public.trigger_qos_test_result();


--
-- Name: radio_cell trigger_radio_cell; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_radio_cell BEFORE INSERT ON public.radio_cell FOR EACH ROW EXECUTE FUNCTION public.trigger_radio_cell();


--
-- Name: test trigger_test; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_test BEFORE INSERT OR UPDATE ON public.test FOR EACH ROW EXECUTE FUNCTION public.trigger_test();


--
-- Name: test_location trigger_test_location2; Type: TRIGGER; Schema: public; Owner: rmbt
--

CREATE TRIGGER trigger_test_location2 BEFORE INSERT OR UPDATE ON public.test_location FOR EACH ROW EXECUTE FUNCTION public.trigger_test_location();


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
-- Name: geo_location geo_location_open_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.geo_location
    ADD CONSTRAINT geo_location_open_test_uuid_fkey FOREIGN KEY (open_test_uuid) REFERENCES public.test(open_test_uuid) ON DELETE CASCADE;


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
-- Name: qos_test_result_b qos_test_result_test_uid; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result_b
    ADD CONSTRAINT qos_test_result_test_uid FOREIGN KEY (test_uid) REFERENCES public.test(uid) ON DELETE CASCADE;


--
-- Name: qos_test_result_b qos_test_resultb_qos_test_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.qos_test_result_b
    ADD CONSTRAINT qos_test_resultb_qos_test_uid_fkey FOREIGN KEY (qos_test_uid) REFERENCES public.qos_test_objective(uid) ON DELETE CASCADE;


--
-- Name: radio_signal_location radio_signal_location_open_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT radio_signal_location_open_test_uuid_fkey FOREIGN KEY (open_test_uuid) REFERENCES public.test(open_test_uuid) ON DELETE CASCADE;


--
-- Name: radio_signal_location radio_signal_location_radio_geo_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT radio_signal_location_radio_geo_location_id_fkey FOREIGN KEY (last_geo_location_uuid) REFERENCES public.geo_location(geo_location_uuid) ON DELETE CASCADE;


--
-- Name: radio_signal_location radio_signal_location_radio_signal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT radio_signal_location_radio_signal_id_fkey FOREIGN KEY (last_radio_signal_uuid) REFERENCES public.radio_signal(radio_signal_uuid) ON DELETE CASCADE;


--
-- Name: radio_signal_location radio_signal_location_signal_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal_location
    ADD CONSTRAINT radio_signal_location_signal_id_fkey FOREIGN KEY (last_signal_uuid) REFERENCES public.signal(signal_uuid) ON DELETE CASCADE;


--
-- Name: radio_signal radio_signal_open_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.radio_signal
    ADD CONSTRAINT radio_signal_open_test_uuid_fkey FOREIGN KEY (open_test_uuid) REFERENCES public.test(open_test_uuid) ON DELETE CASCADE;


--
-- Name: signal signal_open_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.signal
    ADD CONSTRAINT signal_open_test_uuid_fkey FOREIGN KEY (open_test_uuid) REFERENCES public.test(open_test_uuid) ON DELETE CASCADE;


--
-- Name: test test_client_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test
    ADD CONSTRAINT test_client_id_fkey FOREIGN KEY (client_id) REFERENCES public.client(uid) ON DELETE CASCADE;


--
-- Name: test_location test_location_open_test_uuid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: rmbt
--

ALTER TABLE ONLY public.test_location
    ADD CONSTRAINT test_location_open_test_uuid_fkey FOREIGN KEY (open_test_uuid) REFERENCES public.test(open_test_uuid) ON DELETE CASCADE;


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
-- Name: job cron_job_policy; Type: POLICY; Schema: cron; Owner: postgres
--

-- CREATE POLICY cron_job_policy ON cron.job USING ((username = CURRENT_USER));


--
-- Name: job_run_details cron_job_run_details_policy; Type: POLICY; Schema: cron; Owner: postgres
--

-- CREATE POLICY cron_job_run_details_policy ON cron.job_run_details USING ((username = CURRENT_USER));


--
-- Name: job; Type: ROW SECURITY; Schema: cron; Owner: postgres
--

ALTER TABLE cron.job ENABLE ROW LEVEL SECURITY;

--
-- Name: job_run_details; Type: ROW SECURITY; Schema: cron; Owner: postgres
--

ALTER TABLE cron.job_run_details ENABLE ROW LEVEL SECURITY;

--
-- Name: FUNCTION interpolate_radio_signal_location(in_open_test_uuid uuid); Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON FUNCTION public.interpolate_radio_signal_location(in_open_test_uuid uuid) FROM PUBLIC;
GRANT ALL ON FUNCTION public.interpolate_radio_signal_location(in_open_test_uuid uuid) TO rmbt_group_read_only;
GRANT ALL ON FUNCTION public.interpolate_radio_signal_location(in_open_test_uuid uuid) TO rmbt_group_control;


--
-- Name: FUNCTION interpolate_radio_signal_location_v2(in_open_test_uuid uuid); Type: ACL; Schema: public; Owner: rmbt
--

REVOKE ALL ON FUNCTION public.interpolate_radio_signal_location_v2(in_open_test_uuid uuid) FROM PUBLIC;
GRANT ALL ON FUNCTION public.interpolate_radio_signal_location_v2(in_open_test_uuid uuid) TO rmbt_group_read_only;
GRANT ALL ON FUNCTION public.interpolate_radio_signal_location_v2(in_open_test_uuid uuid) TO rmbt_group_control;


--
-- Name: TABLE job; Type: ACL; Schema: cron; Owner: postgres
--

GRANT SELECT ON TABLE cron.job TO rmbt_group_read_only;


--
-- Name: TABLE job_run_details; Type: ACL; Schema: cron; Owner: postgres
--

GRANT SELECT ON TABLE cron.job_run_details TO rmbt_group_read_only;


--
-- Name: TABLE admin_0_countries; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.admin_0_countries TO rmbt_group_read_only;
GRANT SELECT ON TABLE public.admin_0_countries TO rmbt_control;
GRANT SELECT ON TABLE public.admin_0_countries TO rmbt_group_control;


--
-- Name: TABLE device_map; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.device_map TO rmbt_group_read_only;


--
-- Name: TABLE as2provider; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.as2provider TO rmbt_group_read_only;


--
-- Name: TABLE atraster; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.atraster TO rmbt_group_read_only;


--
-- Name: TABLE atraster100; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.atraster100 TO rmbt_group_read_only;


--
-- Name: TABLE atraster250; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.atraster250 TO rmbt_group_read_only;


--
-- Name: TABLE bev_vgd; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.bev_vgd TO rmbt_group_read_only;


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
-- Name: TABLE cov_mno_fn; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.cov_mno_fn TO rmbt_group_read_only;


--
-- Name: TABLE cov_visible_name; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.cov_visible_name TO rmbt_group_read_only;


--
-- Name: TABLE dhm; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.dhm TO rmbt_group_read_only;


--
-- Name: TABLE dsr; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.dsr TO rmbt_group_read_only;


--
-- Name: TABLE geo_location; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.geo_location TO rmbt_group_read_only;
GRANT ALL ON TABLE public.geo_location TO rmbt_group_control;


--
-- Name: SEQUENCE geo_location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT,USAGE ON SEQUENCE public.geo_location_uid_seq TO rmbt_group_control;
GRANT SELECT ON SEQUENCE public.geo_location_uid_seq TO rmbt_group_read_only;


--
-- Name: TABLE json_sender; Type: ACL; Schema: public; Owner: rmbt
--

GRANT UPDATE ON TABLE public.json_sender TO rmbt_group_control;


--
-- Name: TABLE link4net; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.link4net TO rmbt_group_read_only;


--
-- Name: TABLE linknet; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.linknet TO rmbt_group_read_only;


--
-- Name: TABLE linknet_names; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.linknet_names TO rmbt_group_read_only;


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
-- Name: TABLE qoe_classification; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qoe_classification TO rmbt_group_read_only;


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
-- Name: TABLE qos_test_result_b; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.qos_test_result_b TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.qos_test_result_b TO rmbt_group_control;


--
-- Name: SEQUENCE qos_test_result_b_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.qos_test_result_b_uid_seq TO rmbt_group_control;


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
GRANT ALL ON TABLE public.radio_signal TO rmbt_group_control;


--
-- Name: TABLE radio_signal_location; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.radio_signal_location TO rmbt_group_read_only;
GRANT ALL ON TABLE public.radio_signal_location TO rmbt_group_control;


--
-- Name: SEQUENCE radio_signal_location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT ALL ON SEQUENCE public.radio_signal_location_uid_seq TO rmbt_group_control;


--
-- Name: SEQUENCE radio_signal_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT,USAGE ON SEQUENCE public.radio_signal_uid_seq TO rmbt_group_control;


--
-- Name: TABLE settings; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.settings TO rmbt_group_read_only;


--
-- Name: TABLE signal; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.signal TO rmbt_group_read_only;
GRANT ALL ON TABLE public.signal TO rmbt_group_control;


--
-- Name: SEQUENCE signal_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT,USAGE ON SEQUENCE public.signal_uid_seq TO rmbt_group_control;


--
-- Name: TABLE speed; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.speed TO rmbt_group_read_only;
GRANT INSERT,UPDATE ON TABLE public.speed TO rmbt_group_control;


--
-- Name: TABLE statistik_austria_gem; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.statistik_austria_gem TO rmbt_group_read_only;


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
-- Name: TABLE test_location; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test_location TO rmbt_group_read_only;
GRANT ALL ON TABLE public.test_location TO rmbt_group_control;


--
-- Name: SEQUENCE test_location_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT,USAGE ON SEQUENCE public.test_location_uid_seq TO rmbt_group_control;
GRANT SELECT ON SEQUENCE public.test_location_uid_seq TO rmbt_group_read_only;


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
-- Name: TABLE test_server_types; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.test_server_types TO rmbt_control;
GRANT SELECT ON TABLE public.test_server_types TO rmbt_group_control;
GRANT SELECT ON TABLE public.test_server_types TO rmbt_group_read_only;


--
-- Name: SEQUENCE test_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT USAGE ON SEQUENCE public.test_uid_seq TO rmbt_group_control;


--
-- Name: SEQUENCE tl2_uid_seq; Type: ACL; Schema: public; Owner: rmbt
--

GRANT ALL ON SEQUENCE public.tl2_uid_seq TO rmbt_group_control;


--
-- Name: TABLE v_dl_bandwidth_per_minute; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.v_dl_bandwidth_per_minute TO rmbt_group_read_only;


--
-- Name: TABLE v_get_replication_delay; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_get_replication_delay TO nagios;


--
-- Name: TABLE v_kibana; Type: ACL; Schema: public; Owner: rmbt
--

GRANT SELECT ON TABLE public.v_kibana TO kibana;
GRANT SELECT ON TABLE public.v_kibana TO rmbt_group_read_only;


--
-- Name: TABLE v_radio_signal_location; Type: ACL; Schema: public; Owner: postgres
--

GRANT SELECT ON TABLE public.v_radio_signal_location TO rmbt_group_read_only;
GRANT SELECT ON TABLE public.v_radio_signal_location TO rmbt;


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
-- Name: within(public.geometry, public.geometry); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.within(public.geometry, public.geometry) RETURNS boolean
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$SELECT ST_Within($1, $2)$_$;


ALTER FUNCTION public.within(public.geometry, public.geometry) OWNER TO postgres;



--
-- PostgreSQL database dump complete
--

