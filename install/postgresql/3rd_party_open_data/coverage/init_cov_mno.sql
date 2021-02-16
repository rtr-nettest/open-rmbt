BEGIN;
 CREATE TABLE "cov_mno" (uid serial,
"operator" varchar (50),
"reference" varchar (50),
"license" varchar (50),
"rfc_date" varchar (50),
"raster" varchar (50),
"dl_normal" bigint,
"ul_normal" bigint,
"dl_max" bigint,
"ul_max" bigint);

ALTER TABLE "cov_mno" ADD PRIMARY KEY (uid);

CREATE INDEX cov_mno_operator_reference_license_raster_idx

    ON cov_mno(operator,reference,license,raster);

CREATE INDEX cov_mno_raster_idx

    ON cov_mno(raster);
COMMIT;
