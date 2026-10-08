-- Creates the databases used by inventory-service and order-service.
-- Run once by the sqlserver-init container; safe to run again.
IF DB_ID('inventory_service') IS NULL
    CREATE DATABASE inventory_service;
GO

IF DB_ID('order_service') IS NULL
    CREATE DATABASE order_service;
GO
