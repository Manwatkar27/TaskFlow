import React, { useState, useEffect } from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
  useNavigate,
} from "react-router-dom";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { ToastContainer, toast } from "react-toastify";
import {
  LogOut,
  Plus,
  CheckCircle,
  Clock,
  User,
  Lock
} from "lucide-react";

import "react-toastify/dist/ReactToastify.css";
import "./App.css";

const API_URL = "https://api-gateway-m72v.onrender.com";

/*  AUTH  */

const Auth = ({ setIsAuthenticated }) => {

  const [isLogin, setIsLogin] = useState(true);

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "ROLE_USER",
    mobile: ""
  });

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const endpoint = isLogin ? "/auth/signin" : "/auth/signup";
      const { data } = await axios.post(API_URL + endpoint, formData);

      if (isLogin) {
        localStorage.setItem("token", data.jwt);
        toast.success("✅ Login successful!");
        setIsAuthenticated(true);
        navigate("/dashboard");
      } else {
        toast.success("✅ Account created! Login now.");
        setIsLogin(true);
      }

    } catch (error) {
      toast.error(error?.response?.data?.message || "❌ Authentication Failed");
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">

        <h2 className="auth-title">
          {isLogin ? "TaskFlow Login" : "Create Account"}
        </h2>

        <form onSubmit={handleSubmit} className="auth-form">

          {!isLogin && (
            <>
              <input
                className="input-field"
                placeholder="Full Name"
                required
                onChange={(e) =>
                  setFormData({ ...formData, fullName: e.target.value })
                }
              />

              <input
                className="input-field"
                placeholder="Mobile"
                required
                onChange={(e) =>
                  setFormData({ ...formData, mobile: e.target.value })
                }
              />

              <select
                className="input-field"
                onChange={(e) =>
                  setFormData({ ...formData, role: e.target.value })
                }
              >
                <option value="ROLE_USER">User</option>
                <option value="ROLE_ADMIN">Admin</option>
              </select>
            </>
          )}

          <input
            type="email"
            className="input-field"
            placeholder="Email"
            required
            onChange={(e) =>
              setFormData({ ...formData, email: e.target.value })
            }
          />

          <input
            type="password"
            className="input-field"
            placeholder="Password"
            required
            onChange={(e) =>
              setFormData({ ...formData, password: e.target.value })
            }
          />

          <button className="btn-primary" type="submit">
            {isLogin ? "Login" : "Register"}
          </button>
        </form>

        <p className="auth-switch" onClick={() => setIsLogin(!isLogin)}>
          {isLogin
            ? "Don't have an account? Register"
            : "Already have an account? Login"}
        </p>

      </div>
    </div>
  );
};

/* TASK CARD  */

const TaskCard = ({ task, role }) => {

  const [showSubmit, setShowSubmit] = useState(false);
  const [githubLink, setGithubLink] = useState("");

  const handleSubmit = async () => {

    try {
      const token = localStorage.getItem("token");

      await axios.post(
        `${API_URL}/api/submissions?task_id=${task.id}&github_link=${githubLink}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      toast.success("✅ Task submitted!");
      setShowSubmit(false);

    } catch {
      toast.error("❌ Submission failed");
    }
  };

  return (
    <div className={`task-card ${task.status === "DONE" ? "border-green" : "border-yellow"}`}>

      <div className="card-header">
        <div>
          <h3 className="task-title">{task.title}</h3>

          <span
            className={`status-badge ${
              task.status === "DONE" ? "badge-green" : "badge-yellow"
            }`}
          >
            {task.status}
          </span>
        </div>

        {task.status === "DONE"
          ? <CheckCircle className="icon-green"/>
          : <Clock className="icon-yellow"/>
        }
      </div>

      <p className="task-desc">
        {task.description}
      </p>

      <div className="card-footer">

        <span className="deadline">
          Deadline: {new Date(task.deadline).toLocaleDateString()}
        </span>

        {role !== "ROLE_ADMIN" && task.status !== "DONE" && (
          <button
            className="btn-link"
            onClick={() => setShowSubmit(!showSubmit)}
          >
            {showSubmit ? "Cancel" : "Submit Work"}
          </button>
        )}
      </div>

      {showSubmit && (
        <div className="submission-box">

          <input
            className="small-input"
            placeholder="GitHub Link"
            onChange={e => setGithubLink(e.target.value)}
          />

          <button
            className="btn-success"
            onClick={handleSubmit}
          >
            Confirm Submission
          </button>

        </div>
      )}
    </div>
  );
};

/*  DASHBOARD  */

const Dashboard = ({ setIsAuthenticated }) => {

  const [tasks, setTasks] = useState([]);
  const [showModal, setShowModal] = useState(false);

  const [taskForm, setTaskForm] = useState({
    title: "",
    description: "",
    deadline: ""
  });

  const navigate = useNavigate();

  const token = localStorage.getItem("token");
  const decoded = token ? jwtDecode(token) : null;

  const role = decoded?.role || "ROLE_USER";

  /* LOAD TASKS */

  const fetchTasks = async () => {

    try {
      const { data } = await axios.get(
        `${API_URL}/api/tasks`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setTasks(data);

    } catch {
      toast.error("⚠️ Session expired");
      handleLogout();
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
    navigate("/");
  };

  /* CREATE TASK */

  const handleCreateTask = async (e) => {
    e.preventDefault();

    try {

      const payload = {
        taskTitle: taskForm.title,
        taskDescription: taskForm.description,
        dueDate: taskForm.deadline
      };

      await axios.post(
        `${API_URL}/api/tasks`,
        payload,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      toast.success("✅ Task created!");

      setShowModal(false);

      setTaskForm({
        title: "",
        description: "",
        deadline: ""
      });

      fetchTasks();

    } catch (error) {
      console.error(error);
      toast.error("❌ Failed to create task");
    }
  };

  return (
    <div className="dashboard-container">

      {/* NAV */}

      <nav className="navbar">

        <div className="logo">
          <Lock size={22}/> TaskFlow
        </div>

        <div className="nav-right">

          <div className="user-info">
            <User size={18}/>
            <span>{role === "ROLE_ADMIN" ? "Admin" : "User"}</span>
          </div>

          <button className="btn-logout" onClick={handleLogout}>
            <LogOut size={18}/>
          </button>

        </div>
      </nav>

      {/* CONTENT */}

      <div className="content-wrapper">

        <div className="content-header">

          <div>
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">Manage your tasks efficiently</p>
          </div>

          {role === "ROLE_ADMIN" && (
            <button
              className="with-icon"
              onClick={() => setShowModal(true)}
            >
              <Plus size={16}/> New Task
            </button>
          )}
        </div>

        <div className="task-grid">
          {tasks.map(task => (
            <TaskCard
              key={task.id}
              task={task}
              role={role}
            />
          ))}
        </div>

        {tasks.length === 0 && (
          <div className="empty-state">
            <p className="empty-title">No tasks found.</p>
            <p className="empty-subtitle">
              Create one to get started.
            </p>
          </div>
        )}
      </div>

      {/* MODAL */}

      {showModal && (

        <div className="modal-overlay">

          <div className="modal-box">

            <h2 className="modal-title">
              Create New Task
            </h2>

            <form
              onSubmit={handleCreateTask}
              className="modal-form"
            >

              <input
                className="input-field"
                placeholder="Task Title"
                value={taskForm.title}
                required
                onChange={(e) =>
                  setTaskForm({ ...taskForm, title: e.target.value })
                }
              />

              <textarea
                className="input-field textarea"
                placeholder="Task Description"
                value={taskForm.description}
                required
                onChange={(e) =>
                  setTaskForm({ ...taskForm, description: e.target.value })
                }
              />

              <input
                type="date"
                className="input-field"
                value={taskForm.deadline}
                required
                onChange={(e) =>
                  setTaskForm({ ...taskForm, deadline: e.target.value })
                }
              />

              <div className="modal-actions">

                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>

                <button type="submit" className="btn-primary">
                  Create Task
                </button>

              </div>

            </form>

          </div>

        </div>
      )}

    </div>
  );
};

/* MAIN APP */

function App() {

  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    setIsAuthenticated(!!localStorage.getItem("token"));
  }, []);

  return (
    <Router>

      <ToastContainer position="top-right"/>

      <Routes>

        <Route
          path="/"
          element={
            !isAuthenticated
              ? <Auth setIsAuthenticated={setIsAuthenticated}/>
              : <Navigate to="/dashboard" replace/>
          }
        />

        <Route
          path="/dashboard"
          element={
            isAuthenticated
              ? <Dashboard setIsAuthenticated={setIsAuthenticated}/>
              : <Navigate to="/" replace/>
          }
        />

      </Routes>

    </Router>
  );
}

export default App;
