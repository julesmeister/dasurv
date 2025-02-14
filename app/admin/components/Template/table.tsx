/* eslint-disable @typescript-eslint/no-empty-object-type */
/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-unused-vars */
'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { DocumentData, QueryDocumentSnapshot } from 'firebase/firestore';
import { Table as AntTable, Button, Spin, Pagination, PaginationProps } from 'antd';
import toast from 'react-hot-toast';

interface Column<T> {
  header: string;
  accessor: string;
  render?: (value: any, row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  initialTotalCount: number;
  itemsPerPage?: number;
  fetchData: (pageSize: number, lastDoc: QueryDocumentSnapshot<DocumentData> | null) => Promise<{
    data: T[];
    lastDoc: QueryDocumentSnapshot<DocumentData> | null;
    totalCount: number;
  }>;
  rowActions?: (row: T) => React.ReactNode;
  loading?: boolean;
  title?: string;
  description?: string;
  expandableContent?: (row: T) => React.ReactNode;
  actions?: React.ReactNode;
}

const Table = <T extends {}>({
  columns,
  data: initialData,
  initialTotalCount,
  itemsPerPage = 10,
  fetchData,
  title,
  description,
  expandableContent,
  actions,
  rowActions
}: TableProps<T>) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [data, setData] = useState<T[]>(initialData);
  const [totalCount, setTotalCount] = useState(initialTotalCount);
  const [lastDoc, setLastDoc] = useState<QueryDocumentSnapshot<DocumentData> | null>(null);
  const [loading, setLoading] = useState(false);
  const [hasInitialized, setHasInitialized] = useState(false);

  const loadPage = useCallback(async (page: number) => {
    if (loading) return; // Prevent loading if already loading

    try {
      setLoading(true);
      const result = await fetchData(itemsPerPage, page === 1 ? null : lastDoc);
      setData(result.data);
      setLastDoc(result.lastDoc);
      setTotalCount(result.totalCount);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error loading data:', error);
      toast.error('Failed to load data');
    } finally {
      setLoading(false);
      setHasInitialized(true);
    }
  }, [fetchData, itemsPerPage, lastDoc, loading]);

  useEffect(() => {
    if (!hasInitialized) {
      loadPage(1);
    }
  }, [loadPage, hasInitialized]);

  const handlePageChange = (page: number) => {
    loadPage(page);
  };

  const renderRowActions = (value: any, row: T) => {
    return rowActions ? rowActions(row) : null;
  };

  const columnsWithRender = columns.map(column => ({
    key: column.accessor,
    title: column.header,
    dataIndex: column.accessor,
    render: (value: any, row: T) => {
      if (column.accessor === 'rowActions') {
        return rowActions ? rowActions(row) : null;
      }
      return column.render ? column.render(value, row) : value;
    },
  }));

  const updatedColumns = [...columnsWithRender, { title: 'Actions', dataIndex: 'rowActions', render: renderRowActions }];

  console.log('Columns:', updatedColumns);
  console.log('Data:', data);

  const itemRender: PaginationProps['itemRender'] = (_, type, originalElement) => {
    if (type === 'prev') {
      return <a>Previous</a>;
    }
    if (type === 'next') {
      return <a>Next</a>;
    }
    return originalElement;
  };

  return (
    <div className="bg-white shadow rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <div className="sm:flex sm:items-center sm:justify-between">
          <div>
            {title && <h3 className="text-lg font-medium leading-6 text-gray-900">{title}</h3>}
            {description && <p className="mt-2 text-sm text-gray-700">{description}</p>}
          </div>
          {actions && (
            <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
              {actions}
            </div>
          )}
        </div>
        <div className="mt-4">
          {loading ? (
            <Spin />
          ) : (
            <AntTable
              columns={updatedColumns}
              dataSource={data}
              pagination={false}
              rowKey={(record: any) => record.id || Math.random().toString()}
              expandable={{
                expandedRowRender: expandableContent ? (row) => expandableContent(row) : undefined,
              }}
            />
          )}
        </div>
        <div className="flex items-center justify-center">
          <Pagination
            current={currentPage}
            total={totalCount}
            pageSize={itemsPerPage}
            itemRender={itemRender}
            onChange={handlePageChange}
            showSizeChanger
            showQuickJumper
            showTotal={(total) => `Total ${total} items`}
            className="mt-4"
          />
        </div>
      </div>
    </div>
  );
};

export default Table;