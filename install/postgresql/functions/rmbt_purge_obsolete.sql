CREATE OR REPLACE FUNCTION public.rmbt_purge_obsolete(age integer)
returns integer
	LANGUAGE plpgsql
AS $function$
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
$function$
;
