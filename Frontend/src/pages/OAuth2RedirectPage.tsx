import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function OAuth2RedirectPage() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (!token) {
      navigate("/login", { replace: true });
      return;
    }

    localStorage.setItem("token", token);


    fetch("http://localhost:8081/api/v1/users/me", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((r) => r.json())
      .then((json) => {
 
        if (json?.data) localStorage.setItem("user", JSON.stringify(json.data));
        navigate("/dashboard", { replace: true });
      })
      .catch(() => {
        navigate("/dashboard", { replace: true });
      });
  }, [navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <p>Signing you in with Google...</p>
    </div>
  );
}