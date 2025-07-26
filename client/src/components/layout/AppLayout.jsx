import Navbar from "@/components/layout/Navbar"
import PageContainer from "@/components/layout/PageContainer"

export default function AppLayout({ children }) {
  return (
      <div className="min-h-screen bg-background text-foreground">
        <Navbar />
        <main className="py-10">
          <PageContainer>
            {children}
          </PageContainer>
        </main>
      </div>
  )
}
