CREATE OR REPLACE FUNCTION public.trigger_geo_location()
	RETURNS trigger
	LANGUAGE plpgsql
AS $function$
	BEGIN

		
    IF (TG_OP = 'INSERT' and new.location is not NULL) then
       new.geom3857=st_setsrid(new.location,3857);
       new.geom4326=st_transform(new.geom3857,4326);
     end if;
    RETURN NEW;

	END;
$function$
;
-- add trigger BEFORE insert

-- create trigger trigger_geo_location before
-- insert
--     on
--     public.geo_location for each row execute function trigger_geo_location()

