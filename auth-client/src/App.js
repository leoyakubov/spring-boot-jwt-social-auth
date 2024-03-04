import React from "react";
import { BrowserRouter, Route, withRouter, Routes } from "react-router-dom";
import Signin from "./signin/Signin";
import Signup from "./signup/Signup";
import Profile from "./profile/Profile";
import "./App.css";

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Profile/>} exact />

          <Route path="/login" element={<Signin />}/>

          <Route path="/signup" element={<Signup />}/>
        </Routes>
      </BrowserRouter>
    </div>
  );
};

export default App;
