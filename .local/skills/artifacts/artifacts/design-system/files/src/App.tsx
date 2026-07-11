import { Button } from './components/ui/button';
import { Badge } from './components/ui/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from './components/ui/card';
import { Input } from './components/ui/input';
import { Label } from './components/ui/label';
import { Switch } from './components/ui/switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';

// The core palette, shown first and in this order. Every design system leads
// with these.
const CORE_SWATCHES = [
  { name: 'primary', className: 'bg-primary' },
  { name: 'secondary', className: 'bg-secondary' },
  { name: 'accent', className: 'bg-accent' },
] as const;

// Supporting roles, shown after the core.
const SUPPORTING_SWATCHES = [
  { name: 'background', className: 'bg-background border' },
  { name: 'foreground', className: 'bg-foreground' },
  { name: 'muted', className: 'bg-muted' },
  { name: 'destructive', className: 'bg-destructive' },
  { name: 'border', className: 'bg-border' },
] as const;

const TYPE_SCALE = [
  { label: 'Display', className: 'text-4xl font-bold' },
  { label: 'Heading', className: 'text-2xl font-semibold' },
  { label: 'Body', className: 'text-base' },
  { label: 'Caption', className: 'text-sm text-muted-foreground' },
] as const;

function Section({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
}) {
  return (
    <section className="space-y-4">
      <div>
        <h2 className="text-xl font-semibold">{title}</h2>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      {children}
    </section>
  );
}

function App() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="mx-auto max-w-4xl space-y-12 px-6 py-12">
        <header className="space-y-2">
          <h1 className="text-3xl font-bold">Design System</h1>
          <p className="text-muted-foreground">
            A living style guide. Tokens live in{' '}
            <code className="rounded bg-muted px-1.5 py-0.5 text-sm">
              tokens.json
            </code>{' '}
            and feed the CSS variables in{' '}
            <code className="rounded bg-muted px-1.5 py-0.5 text-sm">
              src/index.css
            </code>
            .
          </p>
        </header>

        <Section
          title="Colors"
          description="Core palette first (primary, secondary, accent), then supporting roles."
        >
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
              {CORE_SWATCHES.map((swatch) => (
                <div key={swatch.name} className="space-y-2">
                  <div
                    className={`h-16 w-full rounded-md ${swatch.className}`}
                  />
                  <p className="text-sm font-medium">{swatch.name}</p>
                </div>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-5">
              {SUPPORTING_SWATCHES.map((swatch) => (
                <div key={swatch.name} className="space-y-2">
                  <div
                    className={`h-16 w-full rounded-md ${swatch.className}`}
                  />
                  <p className="text-sm font-medium text-muted-foreground">
                    {swatch.name}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </Section>

        <Section
          title="Typography"
          description="Type scale using the token font families."
        >
          <div className="space-y-3">
            {TYPE_SCALE.map((entry) => (
              <p key={entry.label} className={entry.className}>
                {entry.label} — The quick brown fox jumps over the lazy dog
              </p>
            ))}
          </div>
        </Section>

        <Section
          title="Components"
          description="A few shadcn components themed by the tokens."
        >
          <div className="flex flex-wrap gap-3">
            <Button>Primary</Button>
            <Button variant="secondary">Secondary</Button>
            <Button variant="outline">Outline</Button>
            <Button variant="ghost">Ghost</Button>
            <Button variant="destructive">Destructive</Button>
          </div>

          <div className="flex flex-wrap gap-2">
            <Badge>Default</Badge>
            <Badge variant="secondary">Secondary</Badge>
            <Badge variant="outline">Outline</Badge>
            <Badge variant="destructive">Destructive</Badge>
          </div>

          <Card className="max-w-sm">
            <CardHeader>
              <CardTitle>Card</CardTitle>
              <CardDescription>
                Cards use the surface and border tokens.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" placeholder="you@example.com" />
              </div>
              <div className="flex items-center gap-2">
                <Switch id="notifications" />
                <Label htmlFor="notifications">Enable notifications</Label>
              </div>
            </CardContent>
            <CardFooter>
              <Button className="w-full">Save</Button>
            </CardFooter>
          </Card>

          <Tabs defaultValue="preview" className="max-w-sm">
            <TabsList>
              <TabsTrigger value="preview">Preview</TabsTrigger>
              <TabsTrigger value="usage">Usage</TabsTrigger>
            </TabsList>
            <TabsContent value="preview" className="text-sm">
              Tabs inherit the accent and muted tokens.
            </TabsContent>
            <TabsContent value="usage" className="text-sm">
              Import components from this package, e.g.{' '}
              <code className="rounded bg-muted px-1 py-0.5">
                @workspace/&lt;slug&gt;/components/ui
              </code>
              .
            </TabsContent>
          </Tabs>
        </Section>
      </div>
    </div>
  );
}

export default App;
