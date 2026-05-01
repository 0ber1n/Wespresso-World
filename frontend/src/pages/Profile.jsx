import { useState, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { updateEmail, updatePassword, uploadAvatar, getAvatarUrl } from '../services/api';

const inputCls = 'w-full border border-cream-400 bg-cream-50 rounded-xl px-4 py-2.5 text-sm text-brown-900 placeholder-brown-300 focus:outline-none focus:ring-2 focus:ring-forest-600 focus:border-transparent transition';
const labelCls = 'block text-brown-700 text-sm font-medium mb-1.5';
const primaryBtn = 'self-start bg-forest-800 hover:bg-forest-700 disabled:bg-cream-300 disabled:text-brown-400 text-cream-50 px-5 py-2 rounded-xl text-sm font-medium transition-colors shadow-warm';

function Field({ label, children }) {
  return (
    <div>
      <label className={labelCls}>{label}</label>
      {children}
    </div>
  );
}

function Card({ label, children }) {
  return (
    <div className="bg-cream-50 rounded-2xl border border-cream-300 shadow-warm p-8 mb-4">
      <p className="text-xs font-semibold text-brown-400 uppercase tracking-widest mb-5">{label}</p>
      {children}
    </div>
  );
}

export default function Profile() {
  const { user } = useAuth();

  const [avatarSrc, setAvatarSrc] = useState(getAvatarUrl(user.id));
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarMsg, setAvatarMsg] = useState(null);
  const [avatarError, setAvatarError] = useState(null);
  const [avatarLoading, setAvatarLoading] = useState(false);
  const [avatarLoadFailed, setAvatarLoadFailed] = useState(false);
  const fileInputRef = useRef(null);

  const [emailForm, setEmailForm] = useState({ email: '' });
  const [emailMsg, setEmailMsg] = useState(null);
  const [emailError, setEmailError] = useState(null);
  const [emailLoading, setEmailLoading] = useState(false);

  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [pwMsg, setPwMsg] = useState(null);
  const [pwError, setPwError] = useState(null);
  const [pwLoading, setPwLoading] = useState(false);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (!file.name.toLowerCase().match(/\.jpe?g$/)) {
      setAvatarError('Only JPG files are allowed.');
      setAvatarFile(null); setAvatarPreview(null);
      return;
    }
    setAvatarError(null);
    setAvatarFile(file);
    setAvatarPreview(URL.createObjectURL(file));
  };

  const handleAvatarUpload = async () => {
    if (!avatarFile) return;
    setAvatarLoading(true); setAvatarMsg(null); setAvatarError(null);
    try {
      const res = await uploadAvatar(avatarFile);
      setAvatarSrc(`${getAvatarUrl(user.id)}?t=${Date.now()}`);
      setAvatarLoadFailed(false); setAvatarPreview(null); setAvatarFile(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
      setAvatarMsg(res.data || 'Avatar updated.');
    } catch (err) {
      setAvatarError(err.response?.data || 'Upload failed.');
    } finally {
      setAvatarLoading(false);
    }
  };

  const handleEmailSubmit = async (e) => {
    e.preventDefault();
    setEmailMsg(null); setEmailError(null); setEmailLoading(true);
    try {
      await updateEmail({ email: emailForm.email });
      setEmailMsg('Email updated.'); setEmailForm({ email: '' });
    } catch (err) {
      setEmailError(err.response?.data || 'Failed to update email.');
    } finally {
      setEmailLoading(false);
    }
  };

  const handlePwSubmit = async (e) => {
    e.preventDefault();
    setPwMsg(null); setPwError(null);
    if (pwForm.newPassword !== pwForm.confirmPassword) { setPwError('New passwords do not match.'); return; }
    setPwLoading(true);
    try {
      await updatePassword({ currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword });
      setPwMsg('Password updated.'); setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPwError(err.response?.data || 'Failed to update password.');
    } finally {
      setPwLoading(false);
    }
  };

  return (
    <div className="bg-cream-100 min-h-screen">
      <div className="max-w-xl mx-auto px-8 py-12">
        <h1 className="text-4xl font-bold text-brown-900 mb-10" style={{ fontFamily: 'var(--font-family-serif)' }}>
          My Profile
        </h1>

        <Card label="Account">
          <div className="divide-y divide-cream-200 text-sm">
            {[['Username', user.username], ['Email', user.email], ['Role', user.role]].map(([k, v]) => (
              <div key={k} className="flex justify-between py-3">
                <span className="text-brown-400">{k}</span>
                <span className="font-medium text-brown-900 capitalize">{v}</span>
              </div>
            ))}
          </div>
        </Card>

        <Card label="Avatar">
          <div className="flex items-center gap-6">
            {!avatarLoadFailed ? (
              <img
                src={avatarPreview || avatarSrc}
                onError={() => setAvatarLoadFailed(true)}
                alt="Avatar"
                className="w-20 h-20 rounded-full object-cover border-2 border-cream-300 shadow-warm shrink-0"
              />
            ) : (
              <div className="w-20 h-20 rounded-full bg-forest-800 flex items-center justify-center text-cream-50 text-2xl font-bold shrink-0 shadow-warm">
                {user.username[0].toUpperCase()}
              </div>
            )}
            <div className="flex-1 flex flex-col gap-3">
              {avatarMsg && <p className="text-forest-700 text-sm">{avatarMsg}</p>}
              {avatarError && <p className="text-terra-600 text-sm">{avatarError}</p>}
              <input
                ref={fileInputRef}
                type="file"
                accept=".jpg,.jpeg"
                onChange={handleFileChange}
                className="text-xs text-brown-400 file:mr-3 file:py-1.5 file:px-4 file:rounded-lg file:border-0 file:text-xs file:bg-cream-200 file:text-brown-700 hover:file:bg-cream-300 file:cursor-pointer file:border file:border-cream-300 file:transition-colors"
              />
              <button onClick={handleAvatarUpload} disabled={!avatarFile || avatarLoading} className={primaryBtn}>
                {avatarLoading ? 'Uploading…' : 'Upload'}
              </button>
            </div>
          </div>
        </Card>

        <Card label="Change Email">
          <form onSubmit={handleEmailSubmit} className="flex flex-col gap-4">
            {emailMsg && <p className="text-forest-700 text-sm">{emailMsg}</p>}
            {emailError && <p className="text-terra-600 text-sm">{emailError}</p>}
            <Field label="New Email">
              <input type="email" value={emailForm.email} onChange={(e) => setEmailForm({ email: e.target.value })} required placeholder="new@example.com" className={inputCls} />
            </Field>
            <button type="submit" disabled={emailLoading} className={primaryBtn}>
              {emailLoading ? 'Saving…' : 'Update email'}
            </button>
          </form>
        </Card>

        <Card label="Change Password">
          <form onSubmit={handlePwSubmit} className="flex flex-col gap-4">
            {pwMsg && <p className="text-forest-700 text-sm">{pwMsg}</p>}
            {pwError && <p className="text-terra-600 text-sm">{pwError}</p>}
            <Field label="Current Password">
              <input type="password" value={pwForm.currentPassword} onChange={(e) => setPwForm((f) => ({ ...f, currentPassword: e.target.value }))} required className={inputCls} />
            </Field>
            <Field label="New Password">
              <input type="password" value={pwForm.newPassword} onChange={(e) => setPwForm((f) => ({ ...f, newPassword: e.target.value }))} required className={inputCls} />
            </Field>
            <Field label="Confirm New Password">
              <input type="password" value={pwForm.confirmPassword} onChange={(e) => setPwForm((f) => ({ ...f, confirmPassword: e.target.value }))} required className={inputCls} />
            </Field>
            <button type="submit" disabled={pwLoading} className={primaryBtn}>
              {pwLoading ? 'Saving…' : 'Update password'}
            </button>
          </form>
        </Card>
      </div>
    </div>
  );
}
