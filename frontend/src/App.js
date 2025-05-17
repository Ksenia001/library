import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Layout, Typography, Table, Tag, Spin, Alert, Divider, Space } from 'antd';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;

const API_BASE_URL = 'http://localhost:8080/api/v2';

const App = () => {
  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState({ books: true, authors: true, categories: true });
  const [error, setError] = useState({ books: null, authors: null, categories: null });

  const fetchData = async (endpoint, setData, entityName) => {
    try {
      setLoading(prev => ({ ...prev, [entityName]: true }));
      setError(prev => ({ ...prev, [entityName]: null }));
      const response = await axios.get(`${API_BASE_URL}/${endpoint}`);
      setData(response.data);
    } catch (err) {
      setError(prev => ({ ...prev, [entityName]: `Failed to fetch ${entityName}: ${err.message}` }));
      setData([]);
    } finally {
      setLoading(prev => ({ ...prev, [entityName]: false }));
    }
  };

  useEffect(() => {
    fetchData('books', setBooks, 'books');
    fetchData('authors', setAuthors, 'authors');
    fetchData('categories', setCategories, 'categories');
  }, []);

  const bookColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Book Name', dataIndex: 'bookName', key: 'bookName', sorter: (a, b) => a.bookName.localeCompare(b.bookName) },
    { title: 'Author', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => (a.authorName || "").localeCompare(b.authorName || "") },
    {
      title: 'Categories',
      dataIndex: 'categories',
      key: 'categories',
      render: categories => (
          <Space wrap>
            {categories && categories.map(category => <Tag color="blue" key={category}>{category}</Tag>)}
          </Space>
      ),
    },
  ];

  const authorColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Author Name', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => a.authorName.localeCompare(b.authorName) },
    {
      title: 'Books',
      dataIndex: 'books',
      key: 'books',
      render: books => (
          <Space wrap>
            {books && books.map(book => <Tag color="green" key={book}>{book}</Tag>)}
          </Space>
      ),
    },
  ];

  const categoryColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Category Name', dataIndex: 'name', key: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    {
      title: 'Books',
      dataIndex: 'books',
      key: 'books',
      render: books => (
          <Space wrap>
            {books && books.map(book => <Tag color="purple" key={book}>{book}</Tag>)}
          </Space>
      ),
    },
  ];

  const renderTable = (title, data, columns, isLoading, errorMsg) => (
      <>
        <Title level={3}>{title}</Title>
        {isLoading && <Spin tip={`Loading ${title.toLowerCase()}...`}><div style={{height: '100px'}}/></Spin>}
        {errorMsg && <Alert message={errorMsg} type="error" showIcon />}
        {!isLoading && !errorMsg && <Table dataSource={data} columns={columns} rowKey="id" pagination={{ pageSize: 5 }} scroll={{ x: true }} />}
        <Divider />
      </>
  );

  return (
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', alignItems: 'center' }}>
          <Title level={2} style={{ color: 'white', margin: 0 }}>Library Data</Title>
        </Header>
        <Content style={{ padding: '20px 40px' }}>
          <div style={{ background: '#fff', padding: 24, borderRadius: '8px' }}>
            {renderTable('Books', books, bookColumns, loading.books, error.books)}
            {renderTable('Authors', authors, authorColumns, loading.authors, error.authors)}
            {renderTable('Categories', categories, categoryColumns, loading.categories, error.categories)}
          </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          Library UI ©{new Date().getFullYear()}
        </Footer>
      </Layout>
  );
};

export default App;