BEGIN;
 CREATE TABLE "cov_bb_fixed" (uid serial,
"raster" varchar (50),
"operator" varchar (200),
"technology" varchar (50),
"dl_max_mbit" real,
"ul_max_mbit" real,
"date" varchar (50)
);
ALTER TABLE "cov_bb_fixed" ADD PRIMARY KEY (uid);
CREATE INDEX cov_bb_fixed_raster_idx

    ON cov_bb_fixed(raster);
    
COMMIT;
    