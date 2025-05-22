import React from 'react';
import PropTypes from 'prop-types';
import { Typography, Table, Spin, Alert, Space, Button, Input } from 'antd';
import { PlusOutlined } from '@ant-design/icons';

const { Title } = Typography;
const { Search } = Input;

const DEFAULT_TABLE_PAGE_SIZE = 10;

const TableSection = ({
                          data,
                          columns,
                          loadingState,
                          errorState,
                          onAddClick,
                          entityName,
                          searchTerm,
                          onSearchChange,
                      }) => {
    return (
        <div style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', marginBottom: 16 }}>
                {/* Title removed */}
                <Space>
                    <Search
                        placeholder={`Search ${entityName.toLowerCase()}s...`}
                        value={searchTerm}
                        onChange={(e) => onSearchChange(e.target.value)}
                        style={{ width: 250 }}
                        allowClear
                        loading={loadingState}
                    />
                    <Button type="primary" icon={<PlusOutlined />} onClick={onAddClick}>
                        Add {entityName}
                    </Button>
                </Space>
            </div>
            {loadingState && <Spin tip={`Loading ${entityName.toLowerCase()}s...`}><div style={{ height: '150px' }} /></Spin>}
            {errorState && !loadingState && <Alert message={errorState} type="error" showIcon />}
            {!loadingState && !errorState && (
                <Table
                    dataSource={data.map(item => ({ ...item, key: item.id }))}
                    columns={columns}
                    rowKey="id"
                    pagination={{ pageSize: DEFAULT_TABLE_PAGE_SIZE, showSizeChanger: false }}
                    scroll={{ x: 'max-content' }}
                    size="small"
                />
            )}
        </div>
    );
};

TableSection.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    columns: PropTypes.arrayOf(PropTypes.object).isRequired,
    loadingState: PropTypes.bool.isRequired,
    errorState: PropTypes.string,
    onAddClick: PropTypes.func.isRequired,
    entityName: PropTypes.string.isRequired,
    searchTerm: PropTypes.string.isRequired,
    onSearchChange: PropTypes.func.isRequired,
};

TableSection.defaultProps = {
    errorState: null,
};

export default TableSection;