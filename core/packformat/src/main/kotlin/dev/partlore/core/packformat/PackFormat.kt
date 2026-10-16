package dev.partlore.core.packformat

/**
 * The content pack: one read-only SQLite file. The packer writes it; the app reads it.
 * Any change to [ddl] raises [SCHEMA_VERSION]. Citations are always source_id, page, section, ref.
 */
object PackFormat {
    const val SCHEMA_VERSION: Int = 2
    const val MANIFEST_FILE: String = "manifest.json"
    const val CONTENT_LICENSE: String = "CC-BY-SA-4.0"

    fun packFileName(version: String): String = "partlore-content-$version.db"

    val ddl: List<String> =
        listOf(
            "CREATE TABLE meta (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL) WITHOUT ROWID",
            """
            CREATE TABLE category (
                id TEXT PRIMARY KEY NOT NULL, parent_id TEXT, name TEXT NOT NULL, sort INTEGER
            ) WITHOUT ROWID
            """.trimIndent(),
            "CREATE TABLE tag (id TEXT PRIMARY KEY NOT NULL, label TEXT NOT NULL) WITHOUT ROWID",
            """
            CREATE TABLE part (
                id TEXT PRIMARY KEY NOT NULL, kind TEXT NOT NULL, name TEXT NOT NULL, manufacturer TEXT NOT NULL,
                category_id TEXT NOT NULL, summary TEXT NOT NULL, uses_id TEXT
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE status (
                part_id TEXT NOT NULL, file TEXT NOT NULL, level TEXT NOT NULL, checked_by TEXT, checked_on TEXT,
                against TEXT, imported_importer TEXT, imported_source TEXT, imported_ref TEXT, imported_on TEXT,
                note TEXT, PRIMARY KEY (part_id, file)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE part_alias (
                part_id TEXT NOT NULL, alias TEXT NOT NULL, PRIMARY KEY (part_id, alias)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE part_tag (
                part_id TEXT NOT NULL, tag_id TEXT NOT NULL, PRIMARY KEY (part_id, tag_id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE part_related (
                part_id TEXT NOT NULL, related_id TEXT NOT NULL, PRIMARY KEY (part_id, related_id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE source (
                part_id TEXT NOT NULL, id TEXT NOT NULL, type TEXT NOT NULL, title TEXT NOT NULL,
                publisher TEXT NOT NULL, url TEXT NOT NULL, version TEXT, retrieved TEXT NOT NULL, sha256 TEXT,
                license TEXT NOT NULL, PRIMARY KEY (part_id, id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE spec (
                part_id TEXT NOT NULL, is_absolute_max INTEGER NOT NULL, seq INTEGER NOT NULL, key TEXT NOT NULL,
                label TEXT NOT NULL, type TEXT NOT NULL, unit TEXT, min REAL, typ REAL, max REAL, value_number REAL,
                value_text TEXT, condition TEXT, source_id TEXT NOT NULL, page INTEGER, section TEXT, ref TEXT,
                PRIMARY KEY (part_id, is_absolute_max, seq)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE i2c_address (
                part_id TEXT NOT NULL, address INTEGER NOT NULL, is_default INTEGER NOT NULL, select_note TEXT,
                source_id TEXT NOT NULL, page INTEGER, section TEXT, ref TEXT, PRIMARY KEY (part_id, address)
            ) WITHOUT ROWID
            """.trimIndent(),
            "CREATE INDEX i2c_address_by_address ON i2c_address (address)",
            """
            CREATE TABLE header (
                part_id TEXT NOT NULL, id TEXT NOT NULL, label TEXT NOT NULL, type TEXT NOT NULL,
                rows INTEGER NOT NULL, pins_per_row INTEGER NOT NULL, pitch_mm REAL NOT NULL,
                source_id TEXT NOT NULL, page INTEGER, section TEXT, ref TEXT, PRIMARY KEY (part_id, id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE header_edge (
                part_id TEXT NOT NULL, header_id TEXT NOT NULL, seq INTEGER NOT NULL, edge TEXT NOT NULL,
                run TEXT, pins INTEGER NOT NULL, first TEXT NOT NULL, row1 TEXT, ord INTEGER,
                source_id TEXT NOT NULL, page INTEGER, section TEXT, ref TEXT, PRIMARY KEY (part_id, header_id, seq)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE pin (
                part_id TEXT NOT NULL, id TEXT NOT NULL, header_id TEXT NOT NULL, row INTEGER NOT NULL,
                idx INTEGER NOT NULL, chip_name TEXT, module_pad TEXT, board_label TEXT, arduino TEXT,
                direction TEXT NOT NULL, voltage REAL, five_v_tolerant INTEGER, safe TEXT NOT NULL,
                strapping_role TEXT, strapping_must_be TEXT, strapping_at TEXT, note TEXT,
                PRIMARY KEY (part_id, id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE pin_cite (
                part_id TEXT NOT NULL, pin_id TEXT NOT NULL, seq INTEGER NOT NULL, source_id TEXT NOT NULL,
                page INTEGER, section TEXT, ref TEXT, PRIMARY KEY (part_id, pin_id, seq)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE pin_alias (
                part_id TEXT NOT NULL, pin_id TEXT NOT NULL, label TEXT NOT NULL, label_lower TEXT NOT NULL,
                PRIMARY KEY (part_id, pin_id, label)
            ) WITHOUT ROWID
            """.trimIndent(),
            "CREATE INDEX pin_alias_by_label ON pin_alias (label_lower)",
            """
            CREATE TABLE pin_function (
                part_id TEXT NOT NULL, pin_id TEXT NOT NULL, seq INTEGER NOT NULL, type TEXT NOT NULL,
                signal TEXT, is_default INTEGER NOT NULL, PRIMARY KEY (part_id, pin_id, seq)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE gotcha (
                part_id TEXT NOT NULL, id TEXT NOT NULL, severity TEXT NOT NULL, title TEXT NOT NULL,
                body TEXT NOT NULL, PRIMARY KEY (part_id, id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE gotcha_pin (
                part_id TEXT NOT NULL, gotcha_id TEXT NOT NULL, pin_id TEXT NOT NULL,
                PRIMARY KEY (part_id, gotcha_id, pin_id)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE gotcha_cite (
                part_id TEXT NOT NULL, gotcha_id TEXT NOT NULL, seq INTEGER NOT NULL, source_id TEXT NOT NULL,
                page INTEGER, section TEXT, ref TEXT, PRIMARY KEY (part_id, gotcha_id, seq)
            ) WITHOUT ROWID
            """.trimIndent(),
            """
            CREATE TABLE glance (
                part_id TEXT PRIMARY KEY NOT NULL, logic_level_v REAL, five_v_tolerant TEXT, gpio_count INTEGER,
                adc_channels INTEGER, wifi TEXT, bluetooth TEXT
            ) WITHOUT ROWID
            """.trimIndent(),
            "CREATE TABLE article (part_id TEXT PRIMARY KEY NOT NULL, markdown TEXT NOT NULL) WITHOUT ROWID",
            "CREATE VIRTUAL TABLE search USING fts5(part_id UNINDEXED, kind UNINDEXED, ref UNINDEXED, text, " +
                "tokenize = 'trigram')",
        )
}
