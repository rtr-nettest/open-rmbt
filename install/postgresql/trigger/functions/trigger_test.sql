CREATE OR REPLACE FUNCTION public.trigger_test()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
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
  IF ((NEW.location IS NOT NULL) AND (new.location is distinct from old.location)) THEN
        new.geom3857=st_setsrid(new.location,3857); 
        new.geom4326=st_transform(new.geom3857,4326);
  end if;     
   
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
	OR NEW.location IS DISTINCT FROM OLD.location
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
       -- currently inactive to debug performance issue
       -- INSERT INTO radio_signal_location (last_signal_uuid, last_radio_signal_uuid, last_geo_location_uuid, next_geo_location_uuid, open_test_uuid, interpolated_location, "time") select (interpolate_radio_signal_location_v2 (new.open_test_uuid)).*
       -- ON CONFLICT DO NOTHING; -- conflicting rows will be ignored, the remaining will be inserted
    END IF; --location and signal interpolation

    --debugging, should be commented out for production
    --IF (TG_OP = 'INSERT') THEN RAISE warning 'rmbtdebug:TG_OP=% NEW=%',TG_OP, NEW; END IF;
    --IF (TG_OP = 'UPDATE') THEN RAISE warning 'rmbtdebug:TG_OP=% OLD=%',TG_OP, OLD; RAISE warning 'rmbtdebug:TG_OP=% NEW=%',TG_OP, NEW; END IF;
    --debugging end

    RETURN NEW;

END;
$function$
;
