

export default function Home({ isAuthenticated }) {
  return (
      <div>
        <h1 className="text-2xl font-bold">Welcome to AIrchive</h1>
        <p>{isAuthenticated ? "Here's your personalized feed." : "Sign up to explore AI content!"}</p>
      </div>
  )
}