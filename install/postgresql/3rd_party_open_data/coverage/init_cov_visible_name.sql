BEGIN;
CREATE TABLE cov_visible_name (uid serial,
  "operator" varchar (200),
  "visible_name" varchar (50)
);
ALTER TABLE "cov_visible_name" ADD PRIMARY KEY (uid);
CREATE INDEX cov_visible_name_visible_name_idx
    ON cov_visible_name(visible_name);
ALTER TABLE cov_visible_name OWNER TO rmbt;
GRANT SELECT ON TABLE cov_visible_name TO rmbt_group_read_only;
COMMIT;
