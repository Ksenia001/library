import React, { useState, useEffect, useCallback, useMemo } from 'react';
import axios from 'axios';
import {
  Layout, Typography, Table, Tag, Spin, Alert, Divider, Space, Button, Modal, Form, Input, Select, message, Popconfirm
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

const { Header, Content, Footer } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;

const API_BASE_URL = 'http://localhost:8080/api/v2';

const App = () => {

  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [categories, setCategories] = useState([]);

  const [allAuthorsForSelect, setAllAuthorsForSelect] = useState([]);
  const [allCategoriesForSelect, setAllCategoriesForSelect] = useState([]);
  const [allBooksForSelect, setAllBooksForSelect] = useState([]);

  const [loading, setLoading] = useState({ books: true, authors: true, categories: true, selectData: true });
  const [error, setError] = useState({ books: null, authors: null, categories: null, selectData: null });

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


  const fetchData = useCallback(async (endpoint, setData, entityName) => {
    try {
      setLoading(prev => ({ ...prev, [entityName]: true }));
      setError(prev => ({ ...prev, [entityName]: null }));
      const response = await axios.get(`${API_BASE_URL}/${endpoint}`);
      setData(response.data);
    } catch (err) {
      setError(prev => ({ ...prev, [entityName]: `Failed to fetch ${entityName}: ${err.message}` }));
      setData([]);
      message.error(`Failed to load ${entityName}: ${err.response?.data?.message || err.response?.data || err.message}`);
    } finally {
      setLoading(prev => ({ ...prev, [entityName]: false }));
    }
  }, []);

  const fetchAllDataForTables = useCallback(() => {
    fetchData('books', setBooks, 'books');
    fetchData('authors', setAuthors, 'authors');
    fetchData('categories', setCategories, 'categories');
  }, [fetchData]);

  const fetchSelectData = useCallback(async () => {
    setLoading(prev => ({ ...prev, selectData: true }));
    try {
      const [authorsRes, categoriesRes, booksRes] = await Promise.all([
        axios.get(`${API_BASE_URL}/authors`),
        axios.get(`${API_BASE_URL}/categories`),
        axios.get(`${API_BASE_URL}/books`)
      ]);
      setAllAuthorsForSelect(authorsRes.data.map(a => ({ id: a.id, name: a.authorName })));
      setAllCategoriesForSelect(categoriesRes.data.map(c => ({ id: c.id, name: c.name })));
      setAllBooksForSelect(booksRes.data.map(b => ({ id: b.id, name: b.bookName, authorName: b.authorName, categories: b.categories })));
      setError(prev => ({ ...prev, selectData: null }));
    } catch (err) {
      message.error(`Failed to load data for forms: ${err.message}`);
      setError(prev => ({ ...prev, selectData: `Failed to load data for forms: ${err.message}` }));
    } finally {
      setLoading(prev => ({ ...prev, selectData: false }));
    }
  }, []);


  useEffect(() => {
    fetchAllDataForTables();
    fetchSelectData();
  }, [fetchAllDataForTables, fetchSelectData]);

  // ИЗМЕНЕНИЕ: Логика инициализации формы книги перенесена в useEffect
  useEffect(() => {
    if (isBookModalVisible && bookForm) {
      if (editingBook && allAuthorsForSelect.length > 0 && allCategoriesForSelect.length > 0) {
        const author = allAuthorsForSelect.find(a => a.name === editingBook.authorName);
        const categoryIds = editingBook.categories
            ? allCategoriesForSelect
                .filter(c => editingBook.categories.includes(c.name))
                .map(c => c.id)
            : [];
        bookForm.setFieldsValue({
          name: editingBook.bookName,
          authorId: author ? author.id : undefined,
          categoryIds: categoryIds,
        });
      } else if (!editingBook) {
        bookForm.resetFields(); // Для новой книги форма должна быть пустой
      }
    }
  }, [isBookModalVisible, editingBook, bookForm, allAuthorsForSelect, allCategoriesForSelect]);

  // ИЗМЕНЕНИЕ: Логика инициализации формы автора перенесена в useEffect
  useEffect(() => {
    if (isAuthorModalVisible && authorForm) {
      if (editingAuthor) {
        authorForm.setFieldsValue({
          name: editingAuthor.authorName,
        });
      } else {
        authorForm.resetFields();
      }
    }
  }, [isAuthorModalVisible, editingAuthor, authorForm]);

  // ИЗМЕНЕНИЕ: Логика инициализации формы категории перенесена в useEffect
  useEffect(() => {
    if (isCategoryModalVisible && categoryForm) {
      if (editingCategory && allBooksForSelect.length > 0) {
        const categoryBookIds = editingCategory.books
            ? allBooksForSelect
                .filter(b => editingCategory.books.includes(b.name))
                .map(b => b.id)
            : [];
        categoryForm.setFieldsValue({
          name: editingCategory.name,
          bookIds: categoryBookIds,
        });
      } else if (!editingCategory) {
        categoryForm.resetFields();
      }
    }
  }, [isCategoryModalVisible, editingCategory, categoryForm, allBooksForSelect]);


  const handleModalOpen = (setter, form, record = null, recordSetter) => {
    recordSetter(record); // Устанавливаем editingBook, editingAuthor или editingCategory
    // form.resetFields(); // Убрано, т.к. useEffect и destroyOnClose обрабатывают это
    // setTimeout для form.setFieldsValue УБРАН ОТСЮДА
    setter(true); // Открываем модальное окно
  };


  const handleModalCancel = (setter) => {
    setter(false);
    if (setter === setIsBookModalVisible) setEditingBook(null);
    if (setter === setIsAuthorModalVisible) setEditingAuthor(null);
    if (setter === setIsCategoryModalVisible) setEditingCategory(null);
  };

  const handleDelete = async (id, endpoint, entityName) => {
    try {
      await axios.delete(`${API_BASE_URL}/${endpoint}/${id}`);
      message.success(`${entityName} deleted successfully!`);
      fetchAllDataForTables(); // Перезагружаем данные таблиц
      fetchSelectData();    // Перезагружаем данные для селектов
    } catch (err) {
      message.error(`Failed to delete ${entityName}: ${err.response?.data || err.message}`);
    }
  };

  const handleBookFormFinish = async (values) => {
    setIsSubmittingBook(true);
    const payload = {
      authorId: values.authorId,
      name: values.name, // Это имя книги для создания
      categoryIds: values.categoryIds || [],
      // Для обновления DTO ожидает bookName и categoriesIds
      bookName: values.name, // Это имя книги для обновления
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
      fetchAllDataForTables(); // Перезагружаем данные таблиц
      fetchSelectData();    // Перезагружаем данные для селектов
    } catch (err) {
      let errorMsg = "An unexpected error occurred while saving the book.";
      if (err.response) {
        if (typeof err.response.data === 'string' && err.response.data) errorMsg = err.response.data;
        else if (typeof err.response.data === 'object' && err.response.data !== null) {
          const validationErrors = Object.entries(err.response.data).map(([key, value]) => `${key}: ${value}`);
          errorMsg = validationErrors.length > 0 ? validationErrors.join('; ') : JSON.stringify(err.response.data);
        } else if (err.response.statusText) errorMsg = `Error: ${err.response.status} - ${err.response.statusText}`;
      } else if (err.message) errorMsg = err.message;
      message.error(`Failed to save book: ${errorMsg}`, 7);
    } finally {
      setIsSubmittingBook(false);
    }
  };

  // ... (остальной код handleAuthorFormFinish, handleCategoryFormFinish, колонки таблиц, renderTableSection)

  const bookColumns = [
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
            <Popconfirm title="Are you sure to delete this book?" onConfirm={() => handleDelete(record.id, 'books', 'Book')} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  const handleAuthorFormFinish = async (values) => {
    setIsSubmittingAuthor(true);
    const payload = {
      name: values.name, // Для создания
      authorName: values.name, // Для обновления
    };
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
      let errorMsg = "An unexpected error occurred while saving the author.";
      if (err.response) {
        if (typeof err.response.data === 'string' && err.response.data) errorMsg = err.response.data;
        else if (typeof err.response.data === 'object' && err.response.data !== null) errorMsg = JSON.stringify(err.response.data);
        else if (err.response.statusText) errorMsg = `Error: ${err.response.status} - ${err.response.statusText}`;
      } else if (err.message) errorMsg = err.message;
      message.error(`Failed to save author: ${errorMsg}`, 7);
    } finally {
      setIsSubmittingAuthor(false);
    }
  };

  const authorColumns = [
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
            <Popconfirm title="Are you sure to delete this author?" onConfirm={() => handleDelete(record.id, 'authors', 'Author')} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

  const handleCategoryFormFinish = async (values) => {
    setIsSubmittingCategory(true);
    const payload = {
      name: values.name,
      bookIds: values.bookIds || []
    };
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
      let errorMsg = "An unexpected error occurred while saving the category.";
      if (err.response) {
        if (typeof err.response.data === 'string' && err.response.data) errorMsg = err.response.data;
        else if (typeof err.response.data === 'object' && err.response.data !== null) errorMsg = JSON.stringify(err.response.data);
        else if (err.response.statusText) errorMsg = `Error: ${err.response.status} - ${err.response.statusText}`;
      } else if (err.message) errorMsg = err.message;
      message.error(`Failed to save category: ${errorMsg}`, 7);
    } finally {
      setIsSubmittingCategory(false);
    }
  };

  const categoryColumns = [
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
            <Popconfirm title="Are you sure to delete this category?" onConfirm={() => handleDelete(record.id, 'categories', 'Category')} okText="Yes" cancelText="No">
              <Button icon={<DeleteOutlined />} danger />
            </Popconfirm>
          </Space>
      ),
    },
  ];

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
            onCancel={() => handleModalCancel(setIsBookModalVisible)}
            onOk={() => bookForm.submit()}
            destroyOnClose // Важно для сброса состояния формы при закрытии
            confirmLoading={isSubmittingBook}
        >
          <Form
              form={bookForm}
              layout="vertical"
              name="bookForm"
              onFinish={handleBookFormFinish}
              // initialValues можно убрать, так как useEffect теперь управляет значениями
              // preserve={false} // Не нужно с destroyOnClose
          >
            <Form.Item
                name="name"
                label="Book Name"
                rules={[
                  { required: true, message: 'Please input the book name!' },
                  { max: 20, message: 'Book name cannot exceed 20 characters.'}
                ]}
            >
              <Input />
            </Form.Item>
            <Form.Item
                name="authorId"
                label="Author"
                rules={[{ required: true, message: 'Please select an author!' }]}
            >
              <Select placeholder="Select an author" loading={loading.selectData || allAuthorsForSelect.length === 0} allowClear>
                {allAuthorsForSelect.map(author => <Option key={author.id} value={author.id}>{author.name}</Option>)}
              </Select>
            </Form.Item>
            <Form.Item
                name="categoryIds"
                label="Categories"
                rules={[
                  {
                    validator: (_, value) => {
                      if (value && value.length > 5) {
                        return Promise.reject(new Error('A maximum of 5 categories can be selected.'));
                      }
                      return Promise.resolve();
                    }
                  }
                ]}
            >
              <Select mode="multiple" placeholder="Select categories" loading={loading.selectData || allCategoriesForSelect.length === 0} allowClear>
                {allCategoriesForSelect.map(category => <Option key={category.id} value={category.id}>{category.name}</Option>)}
              </Select>
            </Form.Item>
          </Form>
        </Modal>

        {/* Модальное окно для Авторов */}
        <Modal
            title={editingAuthor ? 'Edit Author' : 'Add New Author'}
            open={isAuthorModalVisible}
            onCancel={() => handleModalCancel(setIsAuthorModalVisible)}
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
                  { max: 20, message: 'Author name cannot exceed 20 characters.'}
                ]}
            >
              <Input />
            </Form.Item>
          </Form>
        </Modal>

        {/* Модальное окно для Категорий */}
        <Modal
            title={editingCategory ? 'Edit Category' : 'Add New Category'}
            open={isCategoryModalVisible}
            onCancel={() => handleModalCancel(setIsCategoryModalVisible)}
            onOk={() => categoryForm.submit()}
            destroyOnClose
            confirmLoading={isSubmittingCategory}
        >
          <Form form={categoryForm} layout="vertical" name="categoryForm" onFinish={handleCategoryFormFinish}>
            <Form.Item
                name="name"
                label="Category Name"
                rules={[
                  { required: true, message: 'Please input the category name!' },
                  { max: 20, message: 'Category name cannot exceed 20 characters.'}
                ]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="bookIds" label="Books in this Category">
              <Select mode="multiple" placeholder="Select books" loading={loading.selectData || loading.books || allBooksForSelect.length === 0} allowClear>
                {allBooksForSelect.map(book => <Option key={book.id} value={book.id}>{book.name} {book.authorName ? `(${book.authorName})` : ''}</Option>)}
              </Select>
            </Form.Item>
          </Form>
        </Modal>
      </Layout>
  );
};

export default App;