    -- Migration:
    
    -- alter table test_location add column geom4326 public.geometry(point,4326) null;
    -- alter table test_location add column geom3857 public.geometry(point,3857) null;

    -- fix SRID issue with ne_10m_admin_0_countries
    -- alter table ne_10m_admin_0_countries add column geom4326 public.geometry(multipolygon,4326) null;
    -- update ne_10m_admin_0_countries set geom4326=st_transform(st_setsrid(geom,3857),4326);


CREATE OR REPLACE FUNCTION public.trigger_test_location()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
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
            IF (NEW.gkz_bev IS NOT NULL) THEN -- #659(mod): Austrian communities are more accurate/up-to-date for AT than ne_50_admin_0_countries
                NEW.country_location = 'AT';
            ELSE
                SELECT INTO NEW.country_location iso_a2
                FROM ne_10m_admin_0_countries
                WHERE NEW.geom4326 && geom4326
                  AND Within(NEW.geom4326, geom4326)
                  AND char_length(iso_a2) = 2
                  AND iso_a2 IS DISTINCT FROM 'AT' -- #659: because ne_50_admin_0_countries is inaccurate, do not allow to return 'AT'
                LIMIT 1;
            END IF;

            -- add altitude level from digital terrain model (DTM) #1203
            SELECT INTO NEW.dtm_level ST_Value(rast, (ST_Transform(ST_SetSRID(ST_MakePoint(NEW.geo_long, NEW.geo_lat), 4326), 31287)))
            FROM dhm
            WHERE st_intersects(rast, (ST_Transform(ST_SetSRID(ST_MakePoint(NEW.geo_long, NEW.geo_lat), 4326), 31287)));


        END IF;

    END IF;
    -- end of location post processing

    RETURN NEW;


END;
$function$
;

