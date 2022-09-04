CREATE OR REPLACE FUNCTION public.rmbt_get_country_iso_a2(point public.geometry(point, 4326))
 RETURNS varchar(5)
 LANGUAGE plpgsql
 IMMUTABLE STRICT
AS $function$
DECLARE
  -- ISO3266 two digit country code (e.g. 'US')
  a2 varchar(5);
BEGIN

	-- Example query: select rmbt_get_country_iso_a2(st_setsrid(ST_GeomFromText('POINT(-71.064544 42.28787)'),4326));

	
	select into a2 ac.iso_a2 from admin_0_countries ac where point && ac.geom and within(point,ac.geom) and char_length(iso_a2) = 2;
    return a2;
    
END;
$function$
;

