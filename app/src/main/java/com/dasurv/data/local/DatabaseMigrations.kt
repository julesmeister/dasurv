package com.dasurv.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `appointments` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `scheduledDateTime` INTEGER NOT NULL,
                    `durationMinutes` INTEGER NOT NULL DEFAULT 60,
                    `procedureType` TEXT NOT NULL DEFAULT '',
                    `notes` TEXT NOT NULL DEFAULT '',
                    `status` TEXT NOT NULL DEFAULT 'SCHEDULED',
                    `sessionId` INTEGER DEFAULT NULL,
                    `reminderEnabled` INTEGER NOT NULL DEFAULT 1,
                    `reminderMinutesBefore` INTEGER NOT NULL DEFAULT 30,
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON DELETE SET NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_appointments_clientId` ON `appointments` (`clientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_appointments_sessionId` ON `appointments` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_appointments_scheduledDateTime` ON `appointments` (`scheduledDateTime`)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add new columns to equipment
            db.execSQL("ALTER TABLE `equipment` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'consumable'")
            db.execSQL("ALTER TABLE `equipment` ADD COLUMN `piecesPerPackage` INTEGER NOT NULL DEFAULT 1")

            // Create session_equipment junction table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `session_equipment` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `sessionId` INTEGER NOT NULL,
                    `equipmentId` INTEGER NOT NULL,
                    `quantityUsed` REAL NOT NULL,
                    `costPerPiece` REAL NOT NULL,
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`equipmentId`) REFERENCES `equipment`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_equipment_sessionId` ON `session_equipment` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_equipment_equipmentId` ON `session_equipment` (`equipmentId`)")

            // Create equipment_usage standalone log table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `equipment_usage` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `equipmentId` INTEGER NOT NULL,
                    `quantityUsed` REAL NOT NULL,
                    `date` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`equipmentId`) REFERENCES `equipment`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_usage_equipmentId` ON `equipment_usage` (`equipmentId`)")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `lip_photos` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `photoUri` TEXT NOT NULL,
                    `captureType` TEXT NOT NULL,
                    `followUpInterval` TEXT DEFAULT NULL,
                    `capturedAt` INTEGER NOT NULL,
                    `upperLipColorHex` TEXT DEFAULT NULL,
                    `upperLipCategory` TEXT DEFAULT NULL,
                    `upperLipHue` REAL DEFAULT NULL,
                    `upperLipSaturation` REAL DEFAULT NULL,
                    `upperLipValue` REAL DEFAULT NULL,
                    `lowerLipColorHex` TEXT DEFAULT NULL,
                    `lowerLipCategory` TEXT DEFAULT NULL,
                    `lowerLipHue` REAL DEFAULT NULL,
                    `lowerLipSaturation` REAL DEFAULT NULL,
                    `lowerLipValue` REAL DEFAULT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lip_photos_clientId` ON `lip_photos` (`clientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lip_photos_capturedAt` ON `lip_photos` (`capturedAt`)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `lip_photo_pigments` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `lipPhotoId` INTEGER NOT NULL,
                    `lipZone` TEXT NOT NULL,
                    `pigmentName` TEXT NOT NULL,
                    `pigmentBrand` TEXT NOT NULL,
                    `pigmentColorHex` TEXT NOT NULL,
                    `isRecommended` INTEGER NOT NULL DEFAULT 0,
                    `notes` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`lipPhotoId`) REFERENCES `lip_photos`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_lip_photo_pigments_lipPhotoId` ON `lip_photo_pigments` (`lipPhotoId`)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `sessions` ADD COLUMN `durationSeconds` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sessions` ADD COLUMN `upperLipSeconds` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `sessions` ADD COLUMN `lowerLipSeconds` INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `client_pigment_preferences` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `pigmentName` TEXT NOT NULL,
                    `pigmentBrand` TEXT NOT NULL,
                    `pigmentColorHex` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_pigment_preferences_clientId` ON `client_pigment_preferences` (`clientId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_client_pigment_preferences_clientId_pigmentName_pigmentBrand` ON `client_pigment_preferences` (`clientId`, `pigmentName`, `pigmentBrand`)")
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `pigment_bottles` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `pigmentName` TEXT NOT NULL,
                    `pigmentBrand` TEXT NOT NULL,
                    `colorHex` TEXT NOT NULL,
                    `isCustom` INTEGER NOT NULL DEFAULT 0,
                    `bottleSizeMl` REAL NOT NULL DEFAULT 15.0,
                    `remainingMl` REAL NOT NULL DEFAULT 15.0,
                    `pricePerBottle` REAL NOT NULL DEFAULT 0.0,
                    `pricePerMl` REAL NOT NULL DEFAULT 0.0,
                    `purchaseDate` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    `equipmentId` INTEGER DEFAULT NULL
                )
            """.trimIndent())

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `pigment_bottle_usage` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `bottleId` INTEGER NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `sessionId` INTEGER DEFAULT NULL,
                    `lipArea` TEXT NOT NULL DEFAULT 'BOTH',
                    `mlUsed` REAL NOT NULL,
                    `costAtTimeOfUse` REAL NOT NULL DEFAULT 0.0,
                    `date` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`bottleId`) REFERENCES `pigment_bottles`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pigment_bottle_usage_bottleId` ON `pigment_bottle_usage` (`bottleId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pigment_bottle_usage_clientId` ON `pigment_bottle_usage` (`clientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pigment_bottle_usage_sessionId` ON `pigment_bottle_usage` (`sessionId`)")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes — version bump only
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `lip_photos` ADD COLUMN `upperLipScale` REAL NOT NULL DEFAULT 1.0")
            db.execSQL("ALTER TABLE `lip_photos` ADD COLUMN `lowerLipScale` REAL NOT NULL DEFAULT 1.0")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `client_transactions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `sessionId` INTEGER DEFAULT NULL,
                    `type` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `paymentMethod` TEXT DEFAULT NULL,
                    `date` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL,
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_transactions_clientId` ON `client_transactions` (`clientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_transactions_sessionId` ON `client_transactions` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_transactions_date` ON `client_transactions` (`date`)")
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_category` ON `equipment` (`category`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_type` ON `equipment` (`type`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pigment_bottles_remainingMl` ON `pigment_bottles` (`remainingMl`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pigment_bottles_pigmentName_pigmentBrand` ON `pigment_bottles` (`pigmentName`, `pigmentBrand`)")
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `equipment_purchases` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `equipmentId` INTEGER NOT NULL,
                    `quantity` INTEGER NOT NULL,
                    `totalCost` REAL NOT NULL DEFAULT 0.0,
                    `purchaseDate` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    `purchaseSource` TEXT NOT NULL DEFAULT '',
                    `seller` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`equipmentId`) REFERENCES `equipment`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_purchases_equipmentId` ON `equipment_purchases` (`equipmentId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_purchases_purchaseDate` ON `equipment_purchases` (`purchaseDate`)")
            db.execSQL("ALTER TABLE `equipment` ADD COLUMN `purchaseSource` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `equipment` ADD COLUMN `seller` TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Re-apply potentially missing schema from corrupted dev builds
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `equipment_purchases` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `equipmentId` INTEGER NOT NULL,
                    `quantity` INTEGER NOT NULL,
                    `totalCost` REAL NOT NULL DEFAULT 0.0,
                    `purchaseDate` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL DEFAULT '',
                    `purchaseSource` TEXT NOT NULL DEFAULT '',
                    `seller` TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(`equipmentId`) REFERENCES `equipment`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_purchases_equipmentId` ON `equipment_purchases` (`equipmentId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipment_purchases_purchaseDate` ON `equipment_purchases` (`purchaseDate`)")

            // Add columns if missing (ALTER TABLE will fail if they already exist, so catch)
            try { db.execSQL("ALTER TABLE `equipment` ADD COLUMN `purchaseSource` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `equipment` ADD COLUMN `seller` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `equipment_purchases` ADD COLUMN `purchaseSource` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
            try { db.execSQL("ALTER TABLE `equipment_purchases` ADD COLUMN `seller` TEXT NOT NULL DEFAULT ''") } catch (_: Exception) {}
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // New staff table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `staff` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `phone` TEXT NOT NULL DEFAULT '',
                    `email` TEXT NOT NULL DEFAULT '',
                    `notes` TEXT NOT NULL DEFAULT '',
                    `isActive` INTEGER NOT NULL DEFAULT 1,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // New session_templates table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `session_templates` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `procedure` TEXT NOT NULL DEFAULT '',
                    `notes` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // New session_template_equipment junction table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `session_template_equipment` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `templateId` INTEGER NOT NULL,
                    `equipmentId` INTEGER NOT NULL,
                    `quantity` INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(`templateId`) REFERENCES `session_templates`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`equipmentId`) REFERENCES `equipment`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_template_equipment_templateId` ON `session_template_equipment` (`templateId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_template_equipment_equipmentId` ON `session_template_equipment` (`equipmentId`)")

            // ALTER existing tables
            db.execSQL("ALTER TABLE `equipment` ADD COLUMN `minStockThreshold` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `pigment_bottles` ADD COLUMN `minRemainingMl` REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE `appointments` ADD COLUMN `recurrenceType` TEXT NOT NULL DEFAULT 'NONE'")
            db.execSQL("ALTER TABLE `appointments` ADD COLUMN `recurrenceIntervalDays` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `appointments` ADD COLUMN `recurrenceEndDate` INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE `appointments` ADD COLUMN `parentAppointmentId` INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE `appointments` ADD COLUMN `staffId` INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE `sessions` ADD COLUMN `staffId` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `client_updates` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `clientId` INTEGER NOT NULL,
                    `sessionId` INTEGER DEFAULT NULL,
                    `date` INTEGER NOT NULL,
                    `photoUri` TEXT DEFAULT NULL,
                    `tags` TEXT NOT NULL DEFAULT '[]',
                    `notes` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL,
                    FOREIGN KEY(`clientId`) REFERENCES `clients`(`id`) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_updates_clientId` ON `client_updates` (`clientId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_updates_sessionId` ON `client_updates` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_client_updates_date` ON `client_updates` (`date`)")
        }
    }
}
