/* eslint-disable @typescript-eslint/no-unused-vars */
import Dexie, { Table, Transaction } from 'dexie';
import { Booking } from '../models/booking';
import { InventoryItem } from '../models/inventory';
import { Staff } from '../models/staff';
import { Supplier } from '../models/supplier';
import { Transaction as TransactionModel } from '../models/transaction';
import { Service } from '../models/service';

export class DasurvDatabase extends Dexie {
  appointments!: Table<Booking>;
  appointmentCounts!: Table<{ type: 'upcoming' | 'history' | 'calendar'; count: number; timestamp: number }>;
  inventory!: Table<InventoryItem & { timestamp: number }>;
  inventoryCounts!: Table<{ count: number; timestamp: number; type: 'total' | 'lowStock' }>;
  staffs!: Table<Staff & { timestamp: number; createdAt: number; updatedAt: number }>;
  staffCounts!: Table<{ count: number; timestamp: number; active?: number }>;
  suppliers!: Table<Supplier & { timestamp: number }>;
  supplierCounts!: Table<{ count: number; timestamp: number }>;
  services!: Table<Service & { timestamp: number }>;
  serviceCounts!: Table<{ count: number; timestamp: number }>;
  transactions!: Table<TransactionModel & { timestamp: number }>;
  transactionCounts!: Table<{ count: number; timestamp: number }>;

  constructor() {
    super('dasurvDb');
    this.version(4).stores({
      appointments: '++id, customerName, email, phone, date, time, status, type, therapist, createdAt, updatedAt, timestamp',
      appointmentCounts: '++id, count, type, timestamp',
      inventory: '++id, name, timestamp',
      inventoryCounts: '++id, count, type, timestamp',
      staffs: '++id, name, email, phone, active, createdAt, updatedAt, timestamp',
      staffCounts: '++id, count, active, timestamp',
      suppliers: '++id, name, timestamp',
      supplierCounts: '++id, count, timestamp',
      services: '++id, name, timestamp',
      serviceCounts: '++id, count, timestamp',
      transactions: 'id, date, customerName, timestamp',
      transactionCounts: '++id, count, timestamp'
    });
  }
}

export const db = new DasurvDatabase();

// Cache duration in milliseconds (5 minutes)
export const CACHE_DURATION = 5 * 60 * 1000;

// Function to delete and recreate the database
export const resetDatabase = async () => {
  await db.delete();
  window.location.reload();
};
