// file: frontend/src/App.js
import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import {
  Layout, Typography, Table, Tag, Spin, Alert, Space, Button,
  Modal, Form, Input, Select, message, Popconfirm, Tabs
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, BookOutlined, UserOutlined, AppstoreOutlined } from '@ant-design/icons';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;
const { Option } = Select;
const { TabPane } = Tabs;

const API_BASE_URL = 'http://localhost:8080/api/v2';
const DEFAULT_ERROR_MESSAGE = "An unexpected error occurred.";
const MAX_NAME_LENGTH = 20;
const MAX_CATEGORIES_PER_BOOK = 5;

/**
 * Main application component with tabbed interface for managing library data.
 */
const App = () => {
  const [activeTabKey, setActiveTabKey] = useState('books');

  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [categories, setCategories] = useState([]);

  const [allAuthorsForSelect, setAllAuthorsForSelect] = useState([]);
  const [allCategoriesForSelect, setAllCategoriesForSelect] = useState([]);
  const [allBooksForSelect, setAllBooksForSelect] = useState([]);

  const [loading, setLoading] = useState({
    books: true, authors: true, categories: true, selectData: true
  });
  const [error, setError] = useState({
    books: null, authors: null, categories: null, selectData: null
  });

  const [isBookModalVisible, setIsBookModalVisible] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [bookForm] = Form.useForm();
  const [isSubmittingBook, setIsSubmittingBook] = useState(false);

  const [isAuthorModalVisible, setIsAuthorModalVisible] = useState(false);
  const [editingAuthor, setEditingAuthor] = useState(null);
  const [authorForm] = Form.useForm();
  const [isSubmittingAuthor, setIsSubmittingAuthor] = useState(false);

  const [isCategoryModalVisible, setIsCategoryModalVisible] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [categoryForm] = Form.useForm();
  const [isSubmittingCategory, setIsSubmittingCategory] = useState(false);

  const fetchData = useCallback(async (endpoint, setData, entityName, setLoadingState, setErrorState) => {
    try {
      setLoadingState(prev => ({ ...prev, [entityName]: true }));
      setErrorState(prev => ({ ...prev, [entityName]: null }));
      const response = await axios.get(`${API_BASE_URL}/${endpoint}`);
      setData(response.data || []);
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || DEFAULT_ERROR_MESSAGE;
      setErrorState(prev => ({ ...prev, [entityName]: `Failed to fetch ${entityName}: ${errorMsg}` }));
      setData([]);
      message.error(`Failed to load ${entityName}: ${errorMsg}`);
    } finally {
      setLoadingState(prev => ({ ...prev, [entityName]: false }));
    }
  }, []);

  const fetchAllDataForTables = useCallback(() => {
    fetchData('books', setBooks, 'books', setLoading, setError);
    fetchData('authors', setAuthors, 'authors', setLoading, setError);
    fetchData('categories', setCategories, 'categories', setLoading, setError);
  }, [fetchData]);

  const fetchSelectData = useCallback(async () => {
    setLoading(prev => ({ ...prev, selectData: true }));
    setError(prev => ({ ...prev, selectData: null }));
    try {
      const [authorsRes, categoriesRes, booksRes] = await Promise.all([
        axios.get(`${API_BASE_URL}/authors`),
        axios.get(`${API_BASE_URL}/categories`),
        axios.get(`${API_BASE_URL}/books`)
      ]);
      setAllAuthorsForSelect(
          (authorsRes.data || []).map(a => ({ id: a.id, name: a.authorName }))
      );
      setAllCategoriesForSelect(
          (categoriesRes.data || []).map(c => ({ id: c.id, name: c.name }))
      );
      setAllBooksForSelect(
          (booksRes.data || []).map(b => ({
            id: b.id, name: b.bookName, authorName: b.authorName, categories: b.categories
          }))
      );
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || DEFAULT_ERROR_MESSAGE;
      message.error(`Failed to load data for forms: ${errorMsg}`);
      setError(prev => ({ ...prev, selectData: `Failed to load data for forms: ${errorMsg}` }));
    } finally {
      setLoading(prev => ({ ...prev, selectData: false }));
    }
  }, []);


  useEffect(() => {
    fetchAllDataForTables();
    fetchSelectData();
  }, [fetchAllDataForTables, fetchSelectData]);

  useEffect(() => {
    if (isBookModalVisible && bookForm) {
      if (editingBook && allAuthorsForSelect.length > 0 && allCategoriesForSelect.length > 0) {
        const author = allAuthorsForSelect.find(a => a.name === editingBook.authorName);
        const categoryIds = (editingBook.categories || [])
            .map(catName => allCategoriesForSelect.find(c => c.name === catName)?.id)
            .filter(id => id != null);
        bookForm.setFieldsValue({
          name: editingBook.bookName,
          authorId: author ? author.id : undefined,
          categoryIds: categoryIds,
        });
      } else if (!editingBook) {
        bookForm.resetFields();
      }
    }
  }, [isBookModalVisible, editingBook, bookForm, allAuthorsForSelect, allCategoriesForSelect]);

  useEffect(() => {
    if (isAuthorModalVisible && authorForm) {
      if (editingAuthor) {
        authorForm.setFieldsValue({ name: editingAuthor.authorName });
      } else {
        authorForm.resetFields();
      }
    }
  }, [isAuthorModalVisible, editingAuthor, authorForm]);

  useEffect(() => {
    if (isCategoryModalVisible && categoryForm) {
      if (editingCategory && allBooksForSelect.length > 0) {
        const categoryBookIds = (editingCategory.books || [])
            .map(bookName => allBooksForSelect.find(b => b.name === bookName)?.id)
            .filter(id => id != null);
        categoryForm.setFieldsValue({
          name: editingCategory.name,
          bookIds: categoryBookIds,
        });
      } else if (!editingCategory) {
        categoryForm.resetFields();
      }
    }
  }, [isCategoryModalVisible, editingCategory, categoryForm, allBooksForSelect]);


  const handleModalOpen = (modalSetter, currentForm, record, recordSetter) => {
    recordSetter(record);
    modalSetter(true);
  };

  const handleModalCancel = (modalSetter, recordSetter) => {
    modalSetter(false);
    recordSetter(null);
  };

  const handleDelete = async (id, endpoint, entityName) => {
    try {
      await axios.delete(`${API_BASE_URL}/${endpoint}/${id}`);
      message.success(`${entityName} deleted successfully!`);
      fetchAllDataForTables();
      fetchSelectData();
    } catch (err) {
      const errorMsg = err.response?.data || err.message || `Failed to delete ${entityName}.`;
      message.error(`Failed to delete ${entityName}: ${errorMsg}`);
    }
  };

  const parseBackendError = (err) => {
    if (err.response && err.response.data) {
      if (typeof err.response.data === 'string' && err.response.data) {
        return err.response.data;
      }
      if (Array.isArray(err.response.data)) {
        return err.response.data.join('; ');
      }
      if (typeof err.response.data === 'object' && err.response.data !== null) {
        const validationErrors = Object.values(err.response.data).join('; ');
        if (validationErrors) return validationErrors;
        if (err.response.data.message) return err.response.data.message;
        return JSON.stringify(err.response.data);
      }
      if (err.response.statusText) {
        return `Error ${err.response.status}: ${err.response.statusText}`;
      }
    }
    return err.message || DEFAULT_ERROR_MESSAGE;
  };


  const handleBookFormFinish = async (values) => {
    setIsSubmittingBook(true);
    const payload = {
      authorId: values.authorId,
      name: values.name,
      categoryIds: values.categoryIds || [],
      bookName: values.name,
      categoriesIds: values.categoryIds || []
    };

    try {
      if (editingBook && editingBook.id) {
        await axios.put(`${API_BASE_URL}/books/${editingBook.id}`, payload);
        message.success('Book updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/books`, payload);
        message.success('Book added successfully!');
      }
      setIsBookModalVisible(false);
      setEditingBook(null);
      fetchAllDataForTables();
      fetchSelectData();
    } catch (err) {
      message.error(`Failed to save book: ${parseBackendError(err)}`, 7);
    } finally {
      setIsSubmittingBook(false);
    }
  };

  const bookColumns = [
    // ID column removed
    { title: 'Book Name', dataIndex: 'bookName', key: 'bookName', sorter: (a, b) => a.bookName.localeCompare(b.bookName) },
    { title: 'Author', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => (a.authorName || "").localeCompare(b.authorName || "") },
    {
      title: 'Categories', dataIndex: 'categories', key: 'categories',
      render: cats => <Space wrap>{(cats || []).map(cat => <Tag color="blue" key={cat}>{cat}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 100, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsBookModalVisible, bookForm, record, setEditingBook)} type="primary" size="small" />
            <Popconfirm title="Delete this book?" onConfirm={() => handleDelete(record.id, 'books', 'Book')}>
              <Button icon={<DeleteOutlined />} danger type="primary" size="small" />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  const handleAuthorFormFinish = async (values) => {
    setIsSubmittingAuthor(true);
    const payload = { name: values.name, authorName: values.name };
    try {
      if (editingAuthor && editingAuthor.id) {
        await axios.put(`${API_BASE_URL}/authors/${editingAuthor.id}`, payload);
        message.success('Author updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/authors`, payload);
        message.success('Author added successfully!');
      }
      setIsAuthorModalVisible(false);
      setEditingAuthor(null);
      fetchAllDataForTables();
      fetchSelectData();
    } catch (err) {
      message.error(`Failed to save author: ${parseBackendError(err)}`, 7);
    } finally {
      setIsSubmittingAuthor(false);
    }
  };

  const authorColumns = [
    // ID column removed
    { title: 'Author Name', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => a.authorName.localeCompare(b.authorName) },
    {
      title: 'Books', dataIndex: 'books', key: 'books',
      render: authorBooks => <Space wrap>{(authorBooks || []).map(book => <Tag color="green" key={book}>{book}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 100, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsAuthorModalVisible, authorForm, record, setEditingAuthor)} type="primary" size="small" />
            <Popconfirm title="Delete this author?" onConfirm={() => handleDelete(record.id, 'authors', 'Author')}>
              <Button icon={<DeleteOutlined />} danger type="primary" size="small" />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  const handleCategoryFormFinish = async (values) => {
    setIsSubmittingCategory(true);
    const payload = { name: values.name, bookIds: values.bookIds || [] };
    try {
      if (editingCategory && editingCategory.id) {
        await axios.put(`${API_BASE_URL}/categories/${editingCategory.id}`, payload);
        message.success('Category updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/categories`, payload);
        message.success('Category added successfully!');
      }
      setIsCategoryModalVisible(false);
      setEditingCategory(null);
      fetchAllDataForTables();
      fetchSelectData();
    } catch (err) {
      message.error(`Failed to save category: ${parseBackendError(err)}`, 7);
    } finally {
      setIsSubmittingCategory(false);
    }
  };

  const categoryColumns = [
    // ID column removed
    { title: 'Category Name', dataIndex: 'name', key: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    {
      title: 'Books', dataIndex: 'books', key: 'books',
      render: catBooks => <Space wrap>{(catBooks || []).map(book => <Tag color="purple" key={book}>{book}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 100, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsCategoryModalVisible, categoryForm, record, setEditingCategory)} type="primary" size="small" />
            <Popconfirm title="Delete this category?" onConfirm={() => handleDelete(record.id, 'categories', 'Category')}>
              <Button icon={<DeleteOutlined />} danger type="primary" size="small" />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  const renderTableSection = (title, data, columns, currentLoading, currentError, onAddClick, entityName) => (
      <div style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>{title}</Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={onAddClick}>
            Add {entityName}
          </Button>
        </div>
        {currentLoading && <Spin tip={`Loading ${title.toLowerCase()}...`}><div style={{ height: '150px' }} /></Spin>}
        {currentError && !currentLoading && <Alert message={currentError} type="error" showIcon />}
        {!currentLoading && !currentError && (
            <Table
                dataSource={data.map(item => ({ ...item, key: item.id }))}
                columns={columns}
                rowKey="id"
                pagination={{
                  pageSize: 5,
                  showSizeChanger: false,
                }}
                scroll={{ x: 'max-content' }}
                size="small"
            />
        )}
      </div>
  );

  return (
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', alignItems: 'center', paddingLeft: 20, paddingRight: 20, backgroundColor: '#001529' }}>
          <Title level={3} style={{ color: 'white', margin: 0, flexGrow: 1 }}>
            Library Data Management
          </Title>
        </Header>
        <Content style={{ padding: '20px 40px' }}>
          {(loading.selectData && !allAuthorsForSelect.length && !allCategoriesForSelect.length) &&
              <Spin tip="Loading initial data for forms..." style={{ display: 'block', marginBottom: 16 }} />
          }
          {error.selectData &&
              <Alert message={error.selectData} type="warning" showIcon style={{ marginBottom: 16 }} />
          }

          <Tabs activeKey={activeTabKey} onChange={setActiveTabKey} type="card">
            <TabPane
                tab={<span><BookOutlined /> Books</span>}
                key="books"
            >
              <div style={{ background: '#fff', padding: 24, borderRadius: '0 0 8px 8px' }}>
                {renderTableSection('Books', books, bookColumns, loading.books, error.books, () => handleModalOpen(setIsBookModalVisible, bookForm, null, setEditingBook), 'Book')}
              </div>
            </TabPane>
            <TabPane
                tab={<span><UserOutlined /> Authors</span>}
                key="authors"
            >
              <div style={{ background: '#fff', padding: 24, borderRadius: '0 0 8px 8px' }}>
                {renderTableSection('Authors', authors, authorColumns, loading.authors, error.authors, () => handleModalOpen(setIsAuthorModalVisible, authorForm, null, setEditingAuthor), 'Author')}
              </div>
            </TabPane>
            <TabPane
                tab={<span><AppstoreOutlined /> Categories</span>}
                key="categories"
            >
              <div style={{ background: '#fff', padding: 24, borderRadius: '0 0 8px 8px' }}>
                {renderTableSection('Categories', categories, categoryColumns, loading.categories, error.categories, () => handleModalOpen(setIsCategoryModalVisible, categoryForm, null, setEditingCategory), 'Category')}
              </div>
            </TabPane>
          </Tabs>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          Library UI ©{new Date().getFullYear()}
        </Footer>

        {/* Book Modal */}
        <Modal
            title={editingBook ? 'Edit Book' : 'Add New Book'}
            open={isBookModalVisible}
            onCancel={() => handleModalCancel(setIsBookModalVisible, setEditingBook)}
            onOk={() => bookForm.submit()}
            destroyOnClose
            confirmLoading={isSubmittingBook}
            width={600}
        >
          <Form form={bookForm} layout="vertical" name="bookForm" onFinish={handleBookFormFinish} >
            <Form.Item
                name="name"
                label="Book Name"
                rules={[
                  { required: true, message: 'Please input the book name!' },
                  { max: MAX_NAME_LENGTH, message: `Book name cannot exceed ${MAX_NAME_LENGTH} characters.` }
                ]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="authorId"
                label="Author"
                rules={[{ required: true, message: 'Please select an author!' }]}
            >
              <Select placeholder="Select an author" loading={loading.selectData && allAuthorsForSelect.length === 0} allowClear showSearch filterOption={(input, option) => option.children.toLowerCase().includes(input.toLowerCase())}>
                {allAuthorsForSelect.map(author => <Option key={author.id} value={author.id}>{author.name}</Option>)}
              </Select>
            </Form.Item>
            <Form.Item
                name="categoryIds"
                label="Categories"
                rules={[{
                  validator: (_, value) => (value && value.length > MAX_CATEGORIES_PER_BOOK) ?
                      Promise.reject(new Error(`A maximum of ${MAX_CATEGORIES_PER_BOOK} categories can be selected.`)) :
                      Promise.resolve()
                }]}
            >
              <Select mode="multiple" placeholder="Select categories" loading={loading.selectData && allCategoriesForSelect.length === 0} allowClear showSearch filterOption={(input, option) => option.children.toLowerCase().includes(input.toLowerCase())}>
                {allCategoriesForSelect.map(category => <Option key={category.id} value={category.id}>{category.name}</Option>)}
              </Select>
            </Form.Item>
          </Form>
        </Modal>

        {/* Author Modal */}
        <Modal
            title={editingAuthor ? 'Edit Author' : 'Add New Author'}
            open={isAuthorModalVisible}
            onCancel={() => handleModalCancel(setIsAuthorModalVisible, setEditingAuthor)}
            onOk={() => authorForm.submit()}
            destroyOnClose
            confirmLoading={isSubmittingAuthor}
        >
          <Form form={authorForm} layout="vertical" name="authorForm" onFinish={handleAuthorFormFinish}>
            <Form.Item
                name="name"
                label="Author Name"
                rules={[
                  { required: true, message: 'Please input the author name!' },
                  { max: MAX_NAME_LENGTH, message: `Author name cannot exceed ${MAX_NAME_LENGTH} characters.` }
                ]}
            >
              <Input />
            </Form.Item>
          </Form>
        </Modal>

        {/* Category Modal */}
        <Modal
            title={editingCategory ? 'Edit Category' : 'Add New Category'}
            open={isCategoryModalVisible}
            onCancel={() => handleModalCancel(setIsCategoryModalVisible, setEditingCategory)}
            onOk={() => categoryForm.submit()}
            destroyOnClose
            confirmLoading={isSubmittingCategory}
            width={600}
        >
          <Form form={categoryForm} layout="vertical" name="categoryForm" onFinish={handleCategoryFormFinish}>
            <Form.Item
                name="name"
                label="Category Name"
                rules={[
                  { required: true, message: 'Please input the category name!' },
                  { max: MAX_NAME_LENGTH, message: `Category name cannot exceed ${MAX_NAME_LENGTH} characters.` }
                ]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="bookIds"
                label="Books in this Category"
            >
              <Select mode="multiple" placeholder="Select books" loading={loading.selectData || allBooksForSelect.length === 0} allowClear showSearch filterOption={(input, option) => option.children.toLowerCase().includes(input.toLowerCase())}>
                {allBooksForSelect.map(book => <Option key={book.id} value={book.id}>{book.name} {book.authorName ? `(${book.authorName})` : ''}</Option>)}
              </Select>
            </Form.Item>
          </Form>
        </Modal>
      </Layout>
  );
};

export default App;