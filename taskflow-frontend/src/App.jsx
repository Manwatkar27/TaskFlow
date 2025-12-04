import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ToastContainer, toast } from 'react-toastify';
import { LogOut, Plus, CheckCircle, Clock, User, Lock } from 'lucide-react';
import './App.css'; 

const API_URL = 'https://api-gateway-m72v.onrender.com'; 

// 1. Login / Register Component
const Auth = ({ setIsAuthenticated }) => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '', role: 'ROLE_USER', mobile: '' });
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const endpoint = isLogin ? '/auth/signin' : '/auth/signup';
      const { data } = await axios.post(`${API_URL}${endpoint}`, formData);
      
      if (isLogin) {
        
        localStorage.setItem('token', data.jwt);
        setIsAuthenticated(true);
        
        // Fetch Profile to get the Role immediately
        try {
            const profileRes = await axios.get(`${API_URL}/api/users/profile`, {
                headers: { Authorization: `Bearer ${data.jwt}` }
            });
            localStorage.setItem('role', profileRes.data.role); // Save REAL role
            toast.success(`Welcome, ${profileRes.data.fullName}!`);
            navigate('/dashboard');
        } catch (err) {
            console.error("Failed to fetch profile", err);
            // Fallback if profile fetch fails
            localStorage.setItem('role', 'ROLE_USER'); 
            navigate('/dashboard');
        }

      } else {
        toast.success('Registration Successful! Please Login.');
        setIsLogin(true);
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Authentication Failed');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h2 className="auth-title">{isLogin ? 'TaskFlow Login' : 'Create Account'}</h2>
        <form onSubmit={handleSubmit} className="auth-form">
          {!isLogin && (
            <>
              <input className="input-field" placeholder="Full Name" onChange={e => setFormData({...formData, fullName: e.target.value})} required />
              <input className="input-field" placeholder="Mobile" onChange={e => setFormData({...formData, mobile: e.target.value})} required />
              <select className="input-field" onChange={e => setFormData({...formData, role: e.target.value})}>
                <option value="ROLE_USER">User</option>
                <option value="ROLE_ADMIN">Admin</option>
              </select>
            </>
          )}
          <input className="input-field" type="email" placeholder="Email" onChange={e => setFormData({...formData, email: e.target.value})} required />
          <input className="input-field" type="password" placeholder="Password" onChange={e => setFormData({...formData, password: e.target.value})} required />
          <button className="btn-primary">{isLogin ? 'Login' : 'Register'}</button>
        </form>
        <p className="auth-switch" onClick={() => setIsLogin(!isLogin)}>
          {isLogin ? "Don't have an account? Register" : "Already have an account? Login"}
        </p>
      </div>
    </div>
  );
};

// 2. Task Card Component
const TaskCard = ({ task, role, refreshTasks }) => {
  const [showSubmit, setShowSubmit] = useState(false);
  const [githubLink, setGithubLink] = useState('');

  const handleSubmit = async () => {
    try {
      const token = localStorage.getItem('token');
      await axios.post(`${API_URL}/api/submissions?task_id=${task.id}&github_link=${githubLink}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      toast.success('Task Submitted Successfully!');
      setShowSubmit(false);
    } catch (error) {
      toast.error('Submission Failed');
    }
  };

  return (
    <div className={`task-card ${task.status === 'DONE' ? 'border-green' : 'border-yellow'}`}>
      <div className="card-header">
        <div>
          <h3 className="task-title">{task.title}</h3>
          <span className={`status-badge ${task.status === 'DONE' ? 'badge-green' : 'badge-yellow'}`}>
            {task.status}
          </span>
        </div>
        {task.status === 'DONE' ? <CheckCircle className="icon-green" /> : <Clock className="icon-yellow" />}
      </div>
      <p className="task-desc">{task.description}</p>
      <div className="card-footer">
        <p className="deadline">Deadline: {new Date(task.deadline).toLocaleDateString()}</p>
        
        {/* Only show Submit button if User is NOT Admin and task is NOT done */}
        {role !== 'ROLE_ADMIN' && task.status !== 'DONE' && (
          <button onClick={() => setShowSubmit(!showSubmit)} className="btn-link">
            {showSubmit ? 'Cancel' : 'Submit Work'}
          </button>
        )}
      </div>

      {showSubmit && (
        <div className="submission-box">
          <input 
            type="text" 
            placeholder="Paste GitHub Link here..." 
            className="input-field small-input"
            onChange={(e) => setGithubLink(e.target.value)}
          />
          <button onClick={handleSubmit} className="btn-success">
            Confirm Submission
          </button>
        </div>
      )}
    </div>
  );
};

// 3. Dashboard Component
const Dashboard = ({ setIsAuthenticated }) => {
  const [tasks, setTasks] = useState([]);
  const [showCreate, setShowCreate] = useState(false);
  const [newTask, setNewTask] = useState({ title: '', description: '', imageUrl: '', deadline: '' });
  const navigate = useNavigate();
  
  // Get role from LocalStorage
  const role = localStorage.getItem('role');

  const fetchTasks = async () => {
    try {
      const token = localStorage.getItem('token');
      const { data } = await axios.get(`${API_URL}/api/tasks`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setTasks(data);
    } catch (error) {
      toast.error('Failed to load tasks');
      if (error.response?.status === 403) handleLogout();
    }
  };

  useEffect(() => { fetchTasks(); }, []);

  const handleCreateTask = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      await axios.post(`${API_URL}/api/tasks`, { ...newTask, deadline: new Date(newTask.deadline).toISOString() }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      toast.success('Task Created!');
      setShowCreate(false);
      fetchTasks();
    } catch (error) {
      toast.error('Failed to create task');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setIsAuthenticated(false);
    navigate('/');
  };

  return (
    <div className="dashboard-container">
      <nav className="navbar">
        <h1 className="logo">
          <Lock size={24} /> TaskFlow
        </h1>
        <div className="nav-right">
          <div className="user-info">
            <User size={20} />
            {/* Display Role dynamically */}
            <span>{role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</span>
          </div>
          <button onClick={handleLogout} className="btn-logout">
            <LogOut size={20} />
          </button>
        </div>
      </nav>

      <div className="content-wrapper">
        <div className="content-header">
          <div>
            <h2 className="page-title">Dashboard</h2>
            <p className="page-subtitle">Manage your tasks efficiently</p>
          </div>
          
          {/* Only Show "New Task" button if user is ADMIN */}
          {role === 'ROLE_ADMIN' && (
            <button onClick={() => setShowCreate(true)} className="btn-primary with-icon">
                <Plus size={20} /> New Task
            </button>
          )}
        </div>

        <div className="task-grid">
          {tasks.map(task => (
            <TaskCard key={task.id} task={task} role={role} refreshTasks={fetchTasks} />
          ))}
        </div>

        {tasks.length === 0 && (
          <div className="empty-state">
            <p className="empty-title">No tasks found.</p>
            <p className="empty-subtitle">Create one to get started!</p>
          </div>
        )}
      </div>

      {showCreate && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Create New Task</h3>
            <form onSubmit={handleCreateTask} className="modal-form">
              <input className="input-field" placeholder="Title" onChange={e => setNewTask({...newTask, title: e.target.value})} required />
              <textarea className="input-field textarea" placeholder="Description" rows="3" onChange={e => setNewTask({...newTask, description: e.target.value})} required />
              <input className="input-field" placeholder="Image URL (Optional)" onChange={e => setNewTask({...newTask, imageUrl: e.target.value})} />
              <div>
                <label className="input-label">Deadline</label>
                <input className="input-field" type="datetime-local" onChange={e => setNewTask({...newTask, deadline: e.target.value})} required />
              </div>
              <div className="modal-actions">
                <button type="button" onClick={() => setShowCreate(false)} className="btn-secondary">Cancel</button>
                <button type="submit" className="btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));

  useEffect(() => {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdnjs.cloudflare.com/ajax/libs/ReactToastify/9.1.3/ReactToastify.min.css';
    document.head.appendChild(link);
    return () => { document.head.removeChild(link); };
  }, []);

  return (
    <Router>
      <ToastContainer position="top-right" autoClose={3000} />
      <Routes>
        <Route path="/" element={!isAuthenticated ? <Auth setIsAuthenticated={setIsAuthenticated} /> : <Navigate to="/dashboard" />} />
        <Route path="/dashboard" element={isAuthenticated ? <Dashboard setIsAuthenticated={setIsAuthenticated} /> : <Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;