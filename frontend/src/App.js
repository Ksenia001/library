import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  Layout, Typography, Table, Tag, Spin, Alert, Divider, Space, Button, Modal, Form, Input, Select, message, Popconfirm
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;
const { Option } = Select;

const API_BASE_URL = 'http://localhost:8080/api/v2';

const App = () => {
  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [categories, setCategories] = useState([]);

  const [allAuthorsForSelect, setAllAuthorsForSelect] = useState([]);
  const [allCategoriesForSelect, setAllCategoriesForSelect] = useState([]);
  // Для формы категорий, если нужно будет выбирать книги
  // const [allBooksForSelect, setAllBooksForSelect] = useState([]);


  const [loading, setLoading] = useState({ books: true, authors: true, categories: true, selectData: true });
  const [error, setError] = useState({ books: null, authors: null, categories: null, selectData: null });

  const [isBookModalVisible, setIsBookModalVisible] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [bookForm] = Form.useForm();

  const [isAuthorModalVisible, setIsAuthorModalVisible] = useState(false);
  const [editingAuthor, setEditingAuthor] = useState(null);
  const [authorForm] = Form.useForm();

  const [isCategoryModalVisible, setIsCategoryModalVisible] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [categoryForm] = Form.useForm();

  // --- Общие функции ---
  const fetchData = async (endpoint, setData, entityName, showSuccess = false) => {
    try {
      setLoading(prev => ({ ...prev, [entityName]: true }));
      setError(prev => ({ ...prev, [entityName]: null }));
      const response = await axios.get(`${API_BASE_URL}/${endpoint}`);
      setData(response.data);
      if (showSuccess) message.success(`${entityName.charAt(0).toUpperCase() + entityName.slice(1)} loaded successfully!`);
    } catch (err) {
      setError(prev => ({ ...prev, [entityName]: `Failed to fetch ${entityName}: ${err.message}` }));
      setData([]);
      message.error(`Failed to load ${entityName}: ${err.response?.data?.message || err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, [entityName]: false }));
    }
  };

  const fetchSelectData = async () => {
    setLoading(prev => ({ ...prev, selectData: true }));
    try {
      const [authorsRes, categoriesRes] = await Promise.all([
        axios.get(`${API_BASE_URL}/authors`),
        axios.get(`${API_BASE_URL}/categories`),
        // Если нужно выбирать книги для категорий:
        // axios.get(`${API_BASE_URL}/books`)
      ]);
      setAllAuthorsForSelect(authorsRes.data.map(a => ({ id: a.id, name: a.authorName })));
      setAllCategoriesForSelect(categoriesRes.data.map(c => ({ id: c.id, name: c.name })));
      // setAllBooksForSelect(booksRes.data.map(b => ({ id: b.id, name: b.bookName })));
      setError(prev => ({ ...prev, selectData: null }));
    } catch (err) {
      message.error(`Failed to load data for forms: ${err.message}`);
      setError(prev => ({ ...prev, selectData: `Failed to load data for forms: ${err.message}` }));
    } finally {
      setLoading(prev => ({ ...prev, selectData: false }));
    }
  };


  useEffect(() => {
    fetchData('books', setBooks, 'books');
    fetchData('authors', setAuthors, 'authors');
    fetchData('categories', setCategories, 'categories');
    fetchSelectData();
  }, []);

  const handleModalOpen = (setter, form, record = null, recordSetter) => {
    recordSetter(record);
    if (record) {
      if (form === bookForm) {
        form.setFieldsValue({
          name: record.bookName,
          authorId: allAuthorsForSelect.find(a => a.name === record.authorName)?.id,
          categoryIds: record.categories
              ? allCategoriesForSelect.filter(c => record.categories.includes(c.name)).map(c => c.id)
              : []
        });
      } else if (form === authorForm) {
        form.setFieldsValue({ name: record.authorName });
      } else if (form === categoryForm) {
        form.setFieldsValue({
          name: record.name,
          // bookIds: record.books ? allBooksForSelect.filter(b => record.books.includes(b.name)).map(b => b.id) : []
        });
      }
    } else {
      form.resetFields();
    }
    setter(true);
  };

  const handleModalCancel = (setter, form) => {
    setter(false);
    form.resetFields();
  };

  const handleDelete = async (id, endpoint, entityName, refreshFunc) => {
    try {
      await axios.delete(`${API_BASE_URL}/${endpoint}/${id}`);
      message.success(`${entityName} deleted successfully!`);
      refreshFunc();
    } catch (err) {
      message.error(`Failed to delete ${entityName}: ${err.response?.data || err.message}`);
    }
  };

  // --- Логика для Книг ---
  const refreshBooks = () => fetchData('books', setBooks, 'books');

  const handleBookFormFinish = async (values) => {
    const payload = {
      name: values.name, // BookCreateDto
      bookName: values.name, // BookUpdateDto
      authorId: values.authorId,
      categoryIds: values.categoryIds, // BookCreateDto
      categoriesIds: values.categoryIds // BookUpdateDto
    };

    try {
      if (editingBook) {
        await axios.put(`${API_BASE_URL}/books/${editingBook.id}`, payload);
        message.success('Book updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/books`, payload);
        message.success('Book added successfully!');
      }
      setIsBookModalVisible(false);
      bookForm.resetFields();
      refreshBooks();
      fetchSelectData(); // Обновить данные для селектов, если книга повлияла на категории/авторов (маловероятно для книги)
    } catch (err) {
      const errorMsg = err.response?.data?.message || (err.response?.data && typeof err.response.data === 'object' ? JSON.stringify(err.response.data) : err.message);
      message.error(`Failed to save book: ${errorMsg}`);
    }
  };

  const bookColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Book Name', dataIndex: 'bookName', key: 'bookName', sorter: (a, b) => a.bookName.localeCompare(b.bookName) },
    { title: 'Author', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => (a.authorName || "").localeCompare(b.authorName || "") },
    {
      title: 'Categories', dataIndex: 'categories', key: 'categories',
      render: categories => <Space wrap>{categories?.map(cat => <Tag color="blue" key={cat}>{cat}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 120, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsBookModalVisible, bookForm, record, setEditingBook)} type="primary" />
            <Popconfirm title="Are you sure to delete this book?" onConfirm={() => handleDelete(record.id, 'books', 'Book', refreshBooks)} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  // --- Логика для Авторов ---
  const refreshAuthors = () => fetchData('authors', setAuthors, 'authors');

  const handleAuthorFormFinish = async (values) => {
    const payload = {
      name: values.name, // AuthorCreateDto
      authorName: values.name // AuthorUpdateDto
    };
    try {
      if (editingAuthor) {
        await axios.put(`${API_BASE_URL}/authors/${editingAuthor.id}`, payload);
        message.success('Author updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/authors`, payload);
        message.success('Author added successfully!');
      }
      setIsAuthorModalVisible(false);
      authorForm.resetFields();
      refreshAuthors();
      fetchSelectData(); // Обновить авторов в селектах для книг
    } catch (err) {
      const errorMsg = err.response?.data?.message || (err.response?.data && typeof err.response.data === 'object' ? JSON.stringify(err.response.data) : err.message);
      message.error(`Failed to save author: ${errorMsg}`);
    }
  };

  const authorColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Author Name', dataIndex: 'authorName', key: 'authorName', sorter: (a, b) => a.authorName.localeCompare(b.authorName) },
    {
      title: 'Books', dataIndex: 'books', key: 'books',
      render: books => <Space wrap>{books?.map(book => <Tag color="green" key={book}>{book}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 120, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsAuthorModalVisible, authorForm, record, setEditingAuthor)} type="primary" />
            <Popconfirm title="Are you sure to delete this author?" onConfirm={() => handleDelete(record.id, 'authors', 'Author', refreshAuthors)} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  // --- Логика для Категорий ---
  const refreshCategories = () => fetchData('categories', setCategories, 'categories');

  const handleCategoryFormFinish = async (values) => {
    const payload = {
      name: values.name,
      bookIds: values.bookIds || [] // Убедимся, что bookIds есть, даже если пустой
    };
    try {
      if (editingCategory) {
        await axios.put(`${API_BASE_URL}/categories/${editingCategory.id}`, payload);
        message.success('Category updated successfully!');
      } else {
        await axios.post(`${API_BASE_URL}/categories`, payload);
        message.success('Category added successfully!');
      }
      setIsCategoryModalVisible(false);
      categoryForm.resetFields();
      refreshCategories();
      fetchSelectData(); // Обновить категории в селектах для книг
      refreshBooks(); // Книги могли измениться из-за ассоциаций с категориями
    } catch (err) {
      const errorMsg = err.response?.data?.message || (err.response?.data && typeof err.response.data === 'object' ? JSON.stringify(err.response.data) : err.message);
      message.error(`Failed to save category: ${errorMsg}`);
    }
  };

  const categoryColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, width: 80 },
    { title: 'Category Name', dataIndex: 'name', key: 'name', sorter: (a, b) => a.name.localeCompare(b.name) },
    {
      title: 'Books', dataIndex: 'books', key: 'books',
      render: books => <Space wrap>{books?.map(book => <Tag color="purple" key={book}>{book}</Tag>)}</Space>,
    },
    {
      title: 'Actions', key: 'actions', width: 120, fixed: 'right',
      render: (_, record) => (
          <Space>
            <Button icon={<EditOutlined />} onClick={() => handleModalOpen(setIsCategoryModalVisible, categoryForm, record, setEditingCategory)} type="primary" />
            <Popconfirm title="Are you sure to delete this category?" onConfirm={() => handleDelete(record.id, 'categories', 'Category', refreshCategories)} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  // --- Рендер таблиц и модальных окон ---
  const renderTableSection = (title, data, columns, isLoading, errorMsg, onAdd) => (
      <>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={3} style={{ margin: 0 }}>{title}</Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={onAdd}>
            Add {title.slice(0, -1)}
          </Button>
        </div>
        {isLoading && <Spin tip={`Loading ${title.toLowerCase()}...`}><div style={{height: '100px'}}/></Spin>}
        {errorMsg && !isLoading && <Alert message={errorMsg} type="error" showIcon />}
        {!isLoading && !errorMsg && <Table dataSource={data} columns={columns} rowKey="id" pagination={{ pageSize: 5 }} scroll={{ x: true }} />}
        <Divider />
      </>
  );

  return (
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ display: 'flex', alignItems: 'center' }}>
          <Title level={2} style={{ color: 'white', margin: 0 }}>Library Data Management</Title>
        </Header>
        <Content style={{ padding: '20px 40px' }}>
          {loading.selectData && <Spin tip="Loading initial data for forms..."><div style={{height: '50px'}}/></Spin>}
          {error.selectData && <Alert message={error.selectData} type="warning" showIcon style={{marginBottom: 16}}/>}

          <div style={{ background: '#fff', padding: 24, borderRadius: '8px' }}>
            {renderTableSection('Books', books, bookColumns, loading.books, error.books, () => handleModalOpen(setIsBookModalVisible, bookForm, null, setEditingBook))}
            {renderTableSection('Authors', authors, authorColumns, loading.authors, error.authors, () => handleModalOpen(setIsAuthorModalVisible, authorForm, null, setEditingAuthor))}
            {renderTableSection('Categories', categories, categoryColumns, loading.categories, error.categories, () => handleModalOpen(setIsCategoryModalVisible, categoryForm, null, setEditingCategory))}
          </div>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          Library UI ©{new Date().getFullYear()}
        </Footer>

        {/* Модальное окно для Книг */}
        <Modal
            title={editingBook ? 'Edit Book' : 'Add New Book'}
            open={isBookModalVisible}
            onCancel={() => handleModalCancel(setIsBookModalVisible, bookForm)}
            onOk={() => bookForm.submit()}
            confirmLoading={loading.books} // Можно добавить отдельный флаг для submit
        >
          <Form form={bookForm} layout="vertical" name="bookForm" onFinish={handleBookFormFinish}>
            <Form.Item name="name" label="Book Name" rules={[{ required: true, message: 'Please input the book name!' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="authorId" label="Author" rules={[{ required: true, message: 'Please select an author!' }]}>
              <Select placeholder="Select an author" loading={loading.selectData} allowClear>
                {allAuthorsForSelect.map(author => <Option key={author.id} value={author.id}>{author.name}</Option>)}
              </Select>
            </Form.Item>
            <Form.Item name="categoryIds" label="Categories">
              <Select mode="multiple" placeholder="Select categories" loading={loading.selectData} allowClear>
                {allCategoriesForSelect.map(category => <Option key={category.id} value={category.id}>{category.name}</Option>)}
              </Select>
            </Form.Item>
          </Form>
        </Modal>

        {/* Модальное окно для Авторов */}
        <Modal
            title={editingAuthor ? 'Edit Author' : 'Add New Author'}
            open={isAuthorModalVisible}
            onCancel={() => handleModalCancel(setIsAuthorModalVisible, authorForm)}
            onOk={() => authorForm.submit()}
            confirmLoading={loading.authors}
        >
          <Form form={authorForm} layout="vertical" name="authorForm" onFinish={handleAuthorFormFinish}>
            <Form.Item name="name" label="Author Name" rules={[{ required: true, message: 'Please input the author name!' }]}>
              <Input />
            </Form.Item>
          </Form>
        </Modal>

        {/* Модальное окно для Категорий */}
        <Modal
            title={editingCategory ? 'Edit Category' : 'Add New Category'}
            open={isCategoryModalVisible}
            onCancel={() => handleModalCancel(setIsCategoryModalVisible, categoryForm)}
            onOk={() => categoryForm.submit()}
            confirmLoading={loading.categories}
        >
          <Form form={categoryForm} layout="vertical" name="categoryForm" onFinish={handleCategoryFormFinish}>
            <Form.Item name="name" label="Category Name" rules={[{ required: true, message: 'Please input the category name!' }]}>
              <Input />
            </Form.Item>
            {/*
          // Если нужно добавлять книги к категории при ее создании/редактировании:
          <Form.Item name="bookIds" label="Books in this Category">
            <Select mode="multiple" placeholder="Select books" loading={loading.selectData || loading.books} allowClear>
              {allBooksForSelect.map(book => <Option key={book.id} value={book.id}>{book.name}</Option>)}
            </Select>
          </Form.Item>
          */}
          </Form>
        </Modal>
      </Layout>
  );
};

export default App;